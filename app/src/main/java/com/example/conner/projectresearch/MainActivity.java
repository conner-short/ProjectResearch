package com.example.conner.projectresearch;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends NativeNFCActivity {
    private Switch modeSwitch;
    private Button nfcControlBtn;
    private TextView statusText;
    private TextView switchLabel;
    private EditText messageText;

    @Override
    void onConnect() {
        switch(getNfcMode()) {
            case 0: /* Card em */
                statusText.setText(R.string.card_connected);
                break;

            case 1: /* Reader */
                statusText.setText(R.string.reader_connected);
                break;

            default:
                throw new RuntimeException("Unreachable");
        }

        send(messageText.getText().toString());
    }

    @Override
    void onDisconnect() {
    }

    @Override
    void onNfcReceive(String str) {
        statusText.setText(str);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Initialize the UI */
        setContentView(R.layout.activity_main_debug);

        modeSwitch = (Switch)findViewById(R.id.mode_switch);
        nfcControlBtn = (Button)findViewById(R.id.enable_button);
        statusText = (TextView)findViewById(R.id.status);
        switchLabel = (TextView)findViewById(R.id.switch_label);
        messageText = (EditText)findViewById(R.id.message_text);

        nfcControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNfcEnabled()) {
                    disableNfc();

                    nfcControlBtn.setText(R.string.ui_enable_nfc);
                    statusText.setText("");
                } else {
                    enableNfc();

                    nfcControlBtn.setText(R.string.ui_disable_nfc);
                }
            }
        });

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    /* Reader */
                    setNfcMode(NfcMode.READER);

                    switchLabel.setText(R.string.ui_reader);
                } else {
                    /* Card emulation */
                    setNfcMode(NfcMode.CARD_EM);

                    switchLabel.setText(R.string.ui_card_emulation);
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
}