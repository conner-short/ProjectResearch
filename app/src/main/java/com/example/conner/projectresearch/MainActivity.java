package com.example.conner.projectresearch;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends NativeNFCActivity {
    private Button mReaderButton;
    private Button mCardEmButton;
    private TextView mCardIDView;

    private String toHexString(byte[] bytes) {
        if(bytes.length < 1) {
            return "";
        }

        String hex = "0x";
        String nybble_chars[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C",
            "D", "E", "F"};

        for(int i = 0; i < bytes.length; i++) {
            hex += nybble_chars[(bytes[i] >> 4) & 0xF];
            hex += nybble_chars[bytes[i] & 0xF];
        }

        return hex;
    }

    @Override
    void onCardData(byte[] bytes) {
        CharSequence text = toHexString(bytes);
        mCardIDView.setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initialize the UI */
        setContentView(R.layout.activity_main_debug);

        mReaderButton = (Button) findViewById(R.id.reader_button);
        mCardEmButton = (Button) findViewById(R.id.card_em_button);
        mCardIDView = (TextView) findViewById(R.id.card_id);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void toggleReader(View v) {
        if(mNfcMode != NfcMode.READER) {
            enableReader();
            mCardIDView.setVisibility(View.VISIBLE);
            mReaderButton.setText("Disable Reader");
        } else {
            disableReader();
            mCardIDView.setVisibility(View.INVISIBLE);
            mCardIDView.setText("Card ID");
            mReaderButton.setText("Reader");
        }
    }

    public void toggleCardEm(View v) {
        if(mNfcMode != NfcMode.CARD_EM) {
            enableCardEm();
            mCardEmButton.setText("Disable Card Emulation");
        } else {
            disableCardEm();
            mCardEmButton.setText("Card Emulation");
        }
    }
}