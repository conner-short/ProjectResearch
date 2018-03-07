package com.example.conner.projectresearch;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Class defining NFC activites for main Activity.
 */
public abstract class NativeNFCActivity extends Activity {
    protected enum NfcMode {DISABLED, READER, CARD_EM};

    protected NfcMode mNfcMode;
    private NfcAdapter mNfcAdapter;

    abstract void onCardData(byte[] bytes);

    public final void disableReader() {onNFCDisableReader();}
    public final void disableCardEm() {onNFCDisableCardEm();}

    public final void enableReader() {
        onNFCDisableCardEm();
        onNFCEnableReader();
    }

    public final void enableCardEm() {
        onNFCDisableReader();
        onNFCEnableCardEm();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    protected void onNFCDisableReader() {
        mNfcMode = NfcMode.DISABLED;

        mNfcAdapter.disableReaderMode(this);
    }

    protected void onNFCEnableReader() {
        mNfcMode = NfcMode.READER;

        mNfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(final Tag tag) {
                /* This is running from a worker thread, so any UI works needs to happen on the UI
                 * thread. */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onCardData(tag.getId());
                    }
                });
            }
        }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    protected void onNFCDisableCardEm() {
        mNfcMode = NfcMode.DISABLED;
    }

    protected void onNFCEnableCardEm() {
        mNfcMode = NfcMode.CARD_EM;
    }

    @Override
    protected void onPause() {
        super.onPause();

        onNFCDisableReader();
        onNFCDisableCardEm();
    }
}
