package com.example.conner.projectresearch;

import android.content.Context;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

public class NativeNFCCardService extends HostApduService {
    static final String ACTION_CONNECT = "ACTION_CONNECT";
    static final String ACTION_DISCONNECT = "ACTION_DISCONNECT";
    static final String ACTION_RECEIVED = "ACTION_RECEIVED";

    private List<Byte> rxBuffer = new ArrayList<>();
    private List<Byte> txBuffer = new ArrayList<>();

    private int dataSize;

    @Override
    public byte[] processCommandApdu(byte[] bytes, Bundle bundle) {
        Context c = getApplicationContext();
        LocalBroadcastManager l = LocalBroadcastManager.getInstance(c);

        Intent intent;

        int i, len;

        byte[] txOut, rxBufferBytes;

        switch(NativeNFCCommands.getCmdType(bytes)) {
            case C_SELECT_AID:
                /* Notify app that we've connected */
                intent = new Intent(ACTION_CONNECT);
                l.sendBroadcast(intent);

                rxBuffer.clear();

                return NativeNFCCommands.getResponseOk();

            case C_TX_NO_MORE_DATA:
                for(i = NativeNFCCommands.getCmdTxNoMoreData().length; i < bytes.length; i++) {
                    rxBuffer.add(bytes[i]);
                }

                rxBufferBytes = new byte[rxBuffer.size()];

                for(i = 0; i < rxBuffer.size(); i++) {
                    rxBufferBytes[i] = rxBuffer.get(i);
                }

                /* Send the data buffer to the app */
                intent = new Intent(ACTION_RECEIVED);
                intent.putExtra(Intent.EXTRA_TEXT, new String(rxBufferBytes));
                l.sendBroadcast(intent);

                rxBuffer.clear(); /* Clear the buffer */

                return NativeNFCCommands.getResponseOk();

            case C_TX_MORE_DATA:
                /* Append the data segment of the APDU to the Rx buffer */
                for(i = NativeNFCCommands.getCmdTxMoreData().length; i < bytes.length; i++) {
                    rxBuffer.add(bytes[i]);
                }

                return NativeNFCCommands.getResponseOk();

            case C_RX:
                len = (txBuffer.size() > dataSize) ? dataSize : txBuffer.size();
                txOut = new byte[len + NativeNFCCommands.getResponseMore().length];

                /* Copy len bytes from the buffer to the output */
                System.arraycopy(txBuffer.toArray(), 0, txOut, 0, len);

                /* Remove the sent bytes from the buffer */
                txBuffer = txBuffer.subList(len, txBuffer.size());

                /* Append the status bits depending on remaining data in Tx buffer */
                if(txBuffer.isEmpty()) {
                    System.arraycopy(NativeNFCCommands.getResponseOk(), 0, txOut, len, NativeNFCCommands.getResponseOk().length);
                } else {
                    System.arraycopy(NativeNFCCommands.getResponseMore(), 0, txOut, len, NativeNFCCommands.getResponseMore().length);
                }

                return txOut;

            default:
                return NativeNFCCommands.getResponseError();
        }
    }

    @Override
    public void onDeactivated(int i) {
        Context c = getApplicationContext();
        LocalBroadcastManager l = LocalBroadcastManager.getInstance(c);

        Intent intent = new Intent(ACTION_DISCONNECT);

        l.sendBroadcast(intent);
    }
}
