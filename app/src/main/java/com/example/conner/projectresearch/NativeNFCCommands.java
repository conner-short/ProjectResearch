package com.example.conner.projectresearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.conner.projectresearch.NativeNFCCommands.CMD_TYPE.C_INVALID;
import static com.example.conner.projectresearch.NativeNFCCommands.CMD_TYPE.C_RX;
import static com.example.conner.projectresearch.NativeNFCCommands.CMD_TYPE.C_SELECT_AID;
import static com.example.conner.projectresearch.NativeNFCCommands.CMD_TYPE.C_TX_MORE_DATA;
import static com.example.conner.projectresearch.NativeNFCCommands.CMD_TYPE.C_TX_NO_MORE_DATA;
import static com.example.conner.projectresearch.NativeNFCCommands.RESPONSE_TYPE.R_ERROR;
import static com.example.conner.projectresearch.NativeNFCCommands.RESPONSE_TYPE.R_INVALID;
import static com.example.conner.projectresearch.NativeNFCCommands.RESPONSE_TYPE.R_MORE;
import static com.example.conner.projectresearch.NativeNFCCommands.RESPONSE_TYPE.R_OK;

public class NativeNFCCommands {
    private static byte[] AID                 = {(byte)0xF6, (byte)0x62, (byte)0x58, (byte)0x1E,
                                                 (byte)0x83, (byte)0x64, (byte)0xCA, (byte)0xDF,
                                                 (byte)0x67, (byte)0x49, (byte)0x6A, (byte)0x2B,
                                                 (byte)0x16, (byte)0xD8, (byte)0x4B, (byte)0x5F};

    private static byte[] CMD_SELECT          = {(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00,
                                                 (byte)0x10};
    private static byte[] CMD_TX_NO_MORE_DATA = {(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00};
    private static byte[] CMD_TX_MORE_DATA    = {(byte)0x80, (byte)0x00, (byte)0x01, (byte)0x00};
    private static byte[] CMD_RX              = {(byte)0x80, (byte)0x01, (byte)0x00, (byte)0x00};
    private static byte[] CMD_SET_DATA_SIZE   = {(byte)0x80, (byte)0x02};

    private static byte[] RESPONSE_OK         = {(byte)0x90, (byte)0x00};
    private static byte[] RESPONSE_MORE       = {(byte)0x90, (byte)0x01};
    private static byte[] RESPONSE_ERROR      = {(byte)0xFF, (byte)0x00};

    enum CMD_TYPE {
        C_INVALID,
        C_SELECT_AID,
        C_TX_NO_MORE_DATA,
        C_TX_MORE_DATA,
        C_RX
    }

    enum RESPONSE_TYPE {
        R_INVALID,
        R_ERROR,
        R_OK,
        R_MORE
    }

    static byte[] getCmdSelectAid() {
        byte[] c = new byte[CMD_SELECT.length + AID.length];
        System.arraycopy(CMD_SELECT, 0, c, 0, CMD_SELECT.length);
        System.arraycopy(AID, 0, c, CMD_SELECT.length, AID.length);

        return c;
    }

    static List<byte[]> getCmdSeqTxStr(byte[] str, int maxTransceiveLength) {
        if(str.length == 0) {
            return null;
        }

        List<byte[]> cmdSeq = new ArrayList<byte[]>();

        int i = 0;

        byte[] cmd;

        /* If string is longer than maximum transceive length, fragment it */
        /* Note: it is assumed that CMD_TX_MORE_DATA and CMD_TX_NO_MORE_DATA are the same length */
        while(i < (str.length - (maxTransceiveLength - CMD_TX_MORE_DATA.length))) {
            cmd = new byte[maxTransceiveLength];

            System.arraycopy(CMD_TX_MORE_DATA, 0, cmd, 0, CMD_TX_MORE_DATA.length);
            System.arraycopy(str, i, cmd, CMD_TX_MORE_DATA.length,
                    maxTransceiveLength - CMD_TX_MORE_DATA.length);

            cmdSeq.add(cmd);

            i += maxTransceiveLength - CMD_TX_MORE_DATA.length;
        }

        /* Send the remainder of the string */
        cmd = new byte[CMD_TX_NO_MORE_DATA.length + str.length - i];

        System.arraycopy(CMD_TX_NO_MORE_DATA, 0, cmd, 0, CMD_TX_NO_MORE_DATA.length);
        System.arraycopy(str, i, cmd, CMD_TX_NO_MORE_DATA.length, str.length - i);

        cmdSeq.add(cmd);

        return cmdSeq;
    }

    static byte[] getCmdTxMoreData() {return CMD_TX_MORE_DATA;}
    static byte[] getCmdTxNoMoreData() {return CMD_TX_NO_MORE_DATA;}
    static byte[] getCmdRx() {return CMD_RX;}

    static byte[] getResponseOk() {
        return RESPONSE_OK;
    }
    static byte[] getResponseError() {return RESPONSE_ERROR;}
    static byte[] getResponseMore() {return RESPONSE_MORE;}

    static CMD_TYPE getCmdType(byte[] s) {
        if(Arrays.equals(s, getCmdSelectAid())) {
            return C_SELECT_AID;
        } else if(Arrays.equals(Arrays.copyOfRange(s, 0, CMD_TX_NO_MORE_DATA.length), CMD_TX_NO_MORE_DATA)) {
            return C_TX_NO_MORE_DATA;
        } else if(Arrays.equals(Arrays.copyOfRange(s, 0, CMD_TX_MORE_DATA.length), CMD_TX_MORE_DATA)) {
            return C_TX_MORE_DATA;
        } else if(Arrays.equals(Arrays.copyOfRange(s, 0, CMD_RX.length), CMD_RX)) {
            return C_RX;
        }

        return C_INVALID;
    }

    static RESPONSE_TYPE getResponseType(byte[] s) {
        if(Arrays.equals(s, RESPONSE_OK)) {
            return R_OK;
        } else if(Arrays.equals(s, RESPONSE_ERROR)) {
            return R_ERROR;
        } else if(Arrays.equals(s, RESPONSE_MORE)) {
            return R_MORE;
        }

        return R_INVALID;
    }
}
