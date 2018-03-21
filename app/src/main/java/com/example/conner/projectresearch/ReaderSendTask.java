package com.example.conner.projectresearch;

import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class ReaderSendTask extends AsyncTask<Object, Void, Void> {
    private boolean[] tagConnectedRef;
    private boolean success;

    @Override
    protected Void doInBackground(Object... objects) {
        /* Linter can be ignored here since parameters will always be the same */
        tagConnectedRef = (boolean[])objects[0];
        IsoDep tag = (IsoDep)objects[1];
        List<byte[]> commands = (List<byte[]>)objects[2];

        for(int i = 0; i < commands.get(0).length; i++) {
            Log.d(getClass().getName(), Byte.toString(commands.get(0)[i]));
        }

        byte[] res;

        for(int i = 0; i < commands.size(); i++) {
            /* Send the next command and get a response */
            try {
                res = tag.transceive(commands.get(i));
            } catch(IOException e) {
                success = false;
                return null;
            }

            if(NativeNFCCommands.getResponseType(res) != NativeNFCCommands.RESPONSE_TYPE.R_OK) {
                Log.d(getClass().getName(), "Error in Reader send transaction");
                success = false;
                return null;
            }
        }

        success = true;

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(!success) {
            tagConnectedRef[0] = false;
        }
    }
}
