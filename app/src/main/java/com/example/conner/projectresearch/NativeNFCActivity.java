package com.example.conner.projectresearch;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.JavascriptInterface;

/**
 * Class defining NFC activites for main Activity.
 */
public abstract class NativeNFCActivity extends Activity {
    private NfcAdapter mNfcAdapter;

    abstract void onCardData(byte[] bytes);

    @JavascriptInterface
    public final void disable() {
        onNFCDisable();
    }

    @JavascriptInterface
    public final void enable() {
        onNFCEnable();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    protected void onNFCDisable() {
        mNfcAdapter.disableReaderMode(this);
    }

    protected void onNFCEnable() {
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

    @Override
    protected void onPause() {
        super.onPause();

        onNFCDisable();
    }
}
