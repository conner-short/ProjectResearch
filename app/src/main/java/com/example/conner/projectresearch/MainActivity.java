package com.example.conner.projectresearch;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends NativeNFCActivity {
    private Switch mModeSwitch;
    private Button mNFCControlBtn;
    private TextView mStatusText;
    private TextView mSwitchLabel;

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
    void onReaderConnect() {
        mStatusText.setText(R.string.reader_connected);
    }

    @Override
    void onReaderDisconnect() {
        mStatusText.setText(R.string.reader_disconnected);
    }

    @Override
    void onCardConnect() {
        mStatusText.setText(R.string.card_connected);
    }

    @Override
    void onCardDisconnect() {
        mStatusText.setText(R.string.card_disconnected);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Initialize the UI */
        setContentView(R.layout.activity_main_debug);

        mModeSwitch = (Switch)findViewById(R.id.mode_switch);
        mNFCControlBtn = (Button)findViewById(R.id.enable_button);
        mStatusText = (TextView)findViewById(R.id.status);
        mSwitchLabel = (TextView)findViewById(R.id.switch_label);

        mNFCControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNfcEnabled()) {
                    /* Disable NFC */
                    disableNfc();
                } else {
                    /* Enable NFC */
                    enableNfc();
                }
            }
        });

        mModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    /* Reader */
                    setNfcMode(NfcMode.READER);
                } else {
                    /* Card emulation */
                    setNfcMode(NfcMode.CARD_EM);
                }
            }
        });

        /* Called at the end to prevent triggering NFC mode/status change events before UI elements
         * instantiated */
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    void onNfcModeChange(NfcMode mode) {
        switch(mode) {
            case READER:
                mSwitchLabel.setText(R.string.ui_reader);
                break;

            case CARD_EM:
                mSwitchLabel.setText(R.string.ui_card_emulation);
                break;
        }
    }

    void onNfcStatusChange(boolean nfcIsEnabled) {
        if(nfcIsEnabled) {
            mNFCControlBtn.setText(R.string.ui_disable_nfc);
        } else {
            mNFCControlBtn.setText(R.string.ui_enable_nfc);
            mStatusText.setText("");
        }
    }
}