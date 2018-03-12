package com.example.conner.projectresearch;

import java.util.Arrays;

public class NativeNFCCommands {
    private static byte[] AID = {
            (byte)0xF6, (byte)0x62, (byte)0x58, (byte)0x1E,
            (byte)0x83, (byte)0x64, (byte)0xCA, (byte)0xDF,
            (byte)0x67, (byte)0x49, (byte)0x6A, (byte)0x2B,
            (byte)0x16, (byte)0xD8, (byte)0x4B, (byte)0x5F
    };

    private static byte[] CMD_SELECT = {
            (byte) 0x00, (byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x10
    };

    private static byte[] RESPONSE_OK = {(byte)0x90, (byte)0x00};

    static byte[] getCmdSelectAid() {
        byte[] c = new byte[CMD_SELECT.length + AID.length];
        System.arraycopy(CMD_SELECT, 0, c, 0, CMD_SELECT.length);
        System.arraycopy(AID, 0, c, CMD_SELECT.length, AID.length);

        return c;
    }

    static byte[] getResponseOk() {
        return RESPONSE_OK;
    }

    static byte[] getAID() {
        return AID;
    }

    static boolean isCmdSelectAid(byte[] s) {
        return Arrays.equals(s, getCmdSelectAid());
    }

    static boolean isResponseOk(byte[] s) {
        return Arrays.equals(s, getResponseOk());
    }
}
