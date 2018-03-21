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
import android.webkit.JavascriptInterface;

import java.io.IOException;
import java.util.List;

/**
 * Class defining NFC activites for main Activity.
 */
public abstract class NativeNFCActivity extends Activity {
    protected enum NfcMode {READER, CARD_EM};

    private NfcMode nfcMode;
    private boolean nfcEnabled;
    private NfcAdapter nfcAdapter;

    private IsoDep tagInterface;
    private boolean tagConnected[]; /* This is an array type so that it can be written from another task */

    /* Service connection variables */
    private Intent serviceIntent;
    private Messenger serviceMessenger;
    private IntentFilter serviceIntentFilter;
    private BroadcastReceiver broadcastReceiver;
    private boolean serviceIsBound;

    private ServiceConnection serviceConn;

    private int dataSize;

    private String receivedString;

    /* Implemented by UI class */
    abstract void onConnect();
    abstract void onDisconnect();
    abstract void onNfcReceive(String str);

    @JavascriptInterface
    public final void enable() {}

    @JavascriptInterface
    public final void disable() {}

    @JavascriptInterface
    public final void send(String str) {
        if((!nfcEnabled) || (!tagConnected[0])) {
            return;
        }

        if(nfcMode == NfcMode.READER) {
            /* Get commands for sending string */
            List<byte[]> sendCommands = NativeNFCCommands.getCmdSeqTxStr(str.getBytes(),
                    dataSize);

            new ReaderSendTask().execute(tagConnected, tagInterface, sendCommands);
        } else {
            /* TODO: Send for card em mode */
        }
    }

    @JavascriptInterface
    public final void disconnect() {}

    @JavascriptInterface
    public final void setNfcMode(int mode) {
        /* 0 for card emulation, 1 for reader */
        setNfcMode((mode == 0) ? NfcMode.CARD_EM: NfcMode.READER);
    }

    @JavascriptInterface
    public final int getNfcMode() {
        return (nfcMode == NfcMode.CARD_EM) ? 0 : 1;
    }

    @JavascriptInterface
    protected final boolean isNfcEnabled() {
        return nfcEnabled;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tagConnected = new boolean[1];

        nfcMode = NfcMode.CARD_EM;
        nfcEnabled = false;

        serviceIsBound = false;

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Context c = getApplicationContext();
        serviceIntent = new Intent(c, NativeNFCCardService.class);

        /* Add actions to the service intent filter */
        serviceIntentFilter = new IntentFilter();
        serviceIntentFilter.addAction(NativeNFCCardService.ACTION_CONNECT);
        serviceIntentFilter.addAction(NativeNFCCardService.ACTION_DISCONNECT);
        serviceIntentFilter.addAction(NativeNFCCardService.ACTION_RECEIVED);

        /* Create the broadcast receiver and give it a handler method */
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(NativeNFCCardService.ACTION_CONNECT)) {
                    tagConnected[0] = true;
                    onConnect();
                }

                else if(action.equals(NativeNFCCardService.ACTION_DISCONNECT)) {
                    tagConnected[0] = false;
                    onDisconnect();
                }

                else if(action.equals(NativeNFCCardService.ACTION_RECEIVED)) {
                    receivedString = intent.getStringExtra(Intent.EXTRA_TEXT);
                    Log.d(getClass().getName(), receivedString);
                    onNfcReceive(receivedString);
                }
            }
        };

        /* Give the service connection handler methods */
        serviceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serviceMessenger = new Messenger(iBinder);

                LocalBroadcastManager l = LocalBroadcastManager.getInstance(NativeNFCActivity.this);
                l.registerReceiver(broadcastReceiver, serviceIntentFilter);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

    protected void disableReader() {
        nfcAdapter.disableReaderMode(this);
    }

    protected void enableReader() {
        nfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(final Tag tag) {
                tagConnected[0] = false;

                tagInterface = IsoDep.get(tag);

                if(tagInterface != null) {
                    /* Connect to the tag */
                    try {
                        tagInterface.connect();
                    } catch(IOException e) {
                        Log.d(getClass().getName(), "IsoDep connection to tag failed");
                        return;
                    }

                    /* Save transceive length */
                    dataSize = tagInterface.getMaxTransceiveLength();

                    byte[] res;

                    /* Check for the app AID */
                    try {
                        res = tagInterface.transceive(NativeNFCCommands.getCmdSelectAid());
                    } catch(IOException e) {
                        Log.d(getClass().getName(), "IsoDep AID selection failed");
                        return;
                    }

                    if(NativeNFCCommands.getResponseType(res) == NativeNFCCommands.RESPONSE_TYPE.R_OK) {
                        tagConnected[0] = true;

                        /* Notify UI thread that we're connected */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onConnect();
                            }
                        });
                    }
                }
            }
        }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);

        nfcMode = NfcMode.READER;
    }

    protected void disableCardEm() {
        /* Disconnect from the service */
        if(serviceIsBound) {
            unbindService(serviceConn);

            serviceIsBound = false;
        }
    }

    protected void enableCardEm() {
        bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);

        serviceIsBound = true;
        nfcMode = NfcMode.CARD_EM;
    }

    @Override
    protected void onPause() {
        super.onPause();

        disableNfc();
    }

    protected void setNfcMode(NfcMode m) {
        if(nfcEnabled) {
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
                    throw new RuntimeException("Unreachable");
            }
        }

        nfcMode = m;
    }

    protected void enableNfc() {
        switch(nfcMode) {
            case READER:
                enableReader();
                break;

            case CARD_EM:
                enableCardEm();
                break;
        }

        nfcEnabled = true;
    }

    protected void disableNfc() {
        disableReader();
        disableCardEm();

        nfcEnabled = false;
    }

    public void onReaderSendComplete(boolean success) {
        if(!success) {
        }
    }
}
