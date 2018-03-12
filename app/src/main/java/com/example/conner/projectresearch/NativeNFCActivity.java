package com.example.conner.projectresearch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

/**
 * Class defining NFC activites for main Activity.
 */
public abstract class NativeNFCActivity extends Activity {
    protected enum NfcMode {READER, CARD_EM};

    private NfcMode mNfcMode;
    private boolean mNfcEnabled;
    private NfcAdapter mNfcAdapter;

    /* Service connection variables */
    private Intent mServiceIntent;
    private Messenger mServiceMessenger;
    private IntentFilter mServiceIntentFilter;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mServiceIsBound;

    private ServiceConnection mServiceConn;

    /* Implemented by UI class */
    abstract void onCardConnect();
    abstract void onCardDisconnect();
    abstract void onReaderConnect();
    abstract void onReaderDisconnect();
    abstract void onNfcModeChange(NfcMode mode);
    abstract void onNfcStatusChange(boolean nfcIsEnabled);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNfcMode = NfcMode.CARD_EM;
        mNfcEnabled = false;

        mServiceIsBound = false;

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Context c = getApplicationContext();
        mServiceIntent = new Intent(c, NativeNFCCardService.class);

        /* Add actions to the service intent filter */
        mServiceIntentFilter = new IntentFilter();
        mServiceIntentFilter.addAction(NativeNFCCardService.ACTION_CONNECT);
        mServiceIntentFilter.addAction(NativeNFCCardService.ACTION_DISCONNECT);

        /* Create the broadcast receiver and give it a handler method */
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(NativeNFCCardService.ACTION_CONNECT)) {
                    onCardConnect();
                }

                else if(action.equals(NativeNFCCardService.ACTION_DISCONNECT)) {
                    onCardDisconnect();
                }
            }
        };

        /* Give the service connection handler methods */
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mServiceMessenger = new Messenger(iBinder);

                LocalBroadcastManager l = LocalBroadcastManager.getInstance(NativeNFCActivity.this);
                l.registerReceiver(mBroadcastReceiver, mServiceIntentFilter);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };

        onNfcModeChange(mNfcMode);
        onNfcStatusChange(mNfcEnabled);
    }

    protected void disableReader() {
        mNfcAdapter.disableReaderMode(this);
    }

    protected void enableReader() {
        mNfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(final Tag tag) {
                IsoDep tagInterface = IsoDep.get(tag);

                if(tagInterface != null) {
                    /* Connect to the tag */
                    try {
                        tagInterface.connect();
                    } catch(IOException e) {
                        Log.d(getClass().getName(), "IsoDep connection to tag failed");
                        return;
                    }

                    byte[] res;

                    /* Check for the app AID */
                    try {
                        res = tagInterface.transceive(NativeNFCCommands.getCmdSelectAid());
                    } catch(IOException e) {
                        Log.d(getClass().getName(), "IsoDep AID selection failed");

                        try {
                            tagInterface.close();
                        } catch(IOException f) {
                            Log.d(getClass().getName(), "IsoDep close failed");
                        }

                        return;
                    }

                    if(NativeNFCCommands.isResponseOk(res)) {
                        /* Notify UI thread that we're connected */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onReaderConnect();
                            }
                        });
                    }

                    /* Disconnect */
                    try {
                        tagInterface.close();
                    } catch(IOException e) {
                        Log.d(getClass().getName(), "IsoDep close failed");
                    }

                    /* Notify UI thread that we're disconnected */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onReaderDisconnect();
                        }
                    });

                    return;
                }
            }
        }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);

        mNfcMode = NfcMode.READER;
    }

    protected void disableCardEm() {
        /* Disconnect from the service */
        if(mServiceIsBound) {
            unbindService(mServiceConn);

            mServiceIsBound = false;
        }
    }

    protected void enableCardEm() {
        bindService(mServiceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        mServiceIsBound = true;
        mNfcMode = NfcMode.CARD_EM;
    }

    @Override
    protected void onPause() {
        super.onPause();

        disableNfc();
    }

    protected void setNfcMode(NfcMode m) {
        if(mNfcEnabled) {
            switch(m) {
                case READER:
                    disableCardEm();
                    enableReader();
                    break;

                case CARD_EM:
                    disableReader();
                    enableCardEm();
                    break;

                default:
                    Log.d(getClass().getName(), "Invalid NFC mode, disabling...");

                    disableNfc();
                    mNfcMode = NfcMode.CARD_EM;

                    onNfcModeChange(mNfcMode);

                    return;
            }
        }

        mNfcMode = m;

        onNfcModeChange(mNfcMode);
    }

    protected void enableNfc() {
        switch(mNfcMode) {
            case READER:
                enableReader();
                break;

            case CARD_EM:
                enableCardEm();
                break;
        }

        mNfcEnabled = true;

        onNfcStatusChange(mNfcEnabled);
    }

    protected void disableNfc() {
        disableReader();
        disableCardEm();

        mNfcEnabled = false;

        onNfcStatusChange(mNfcEnabled);
    }

    protected boolean isNfcEnabled() {
        return mNfcEnabled;
    }
}
