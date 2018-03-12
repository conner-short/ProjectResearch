package com.example.conner.projectresearch;

import android.content.Context;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class NativeNFCCardService extends HostApduService {
    public static final String ACTION_CONNECT = "ACTION_CONNECT";
    public static final String ACTION_DISCONNECT = "ACTION_DISCONNECT";

    @Override
    public byte[] processCommandApdu(byte[] bytes, Bundle bundle) {
        Context c = getApplicationContext();
        LocalBroadcastManager l = LocalBroadcastManager.getInstance(c);

        if(NativeNFCCommands.isCmdSelectAid(bytes)) {
            Intent intent = new Intent(ACTION_CONNECT);

            l.sendBroadcast(intent);

            return NativeNFCCommands.getResponseOk();
        }

        return null;
    }

    @Override
    public void onDeactivated(int i) {
        Context c = getApplicationContext();
        LocalBroadcastManager l = LocalBroadcastManager.getInstance(c);

        Intent intent = new Intent(ACTION_DISCONNECT);

        l.sendBroadcast(intent);
    }
}
