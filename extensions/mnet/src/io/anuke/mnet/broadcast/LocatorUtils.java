package io.anuke.mnet.broadcast;

import java.util.Arrays;

class LocatorUtils{


    static final int minMsgLength = 21;

    static boolean startsWithUUID(byte[] message, byte[] uuid){
        for(int i = 0; i < 16; i++){
            if(message[i] != uuid[i]){
                return false;
            }
        }
        return true;
    }

    static byte[] normalizeUUID(byte[] uuid){
        if(uuid.length == 0) throw new RuntimeException("UUID must not have length of 0");
        if(uuid.length == 16) return Arrays.copyOf(uuid, 16);
        byte[] newUUID = new byte[16];
        if(uuid.length < 16){
            for(int i = 0; i < 16; i++){
                newUUID[i] = 1;
            }
            System.arraycopy(uuid, 0, newUUID, 0, uuid.length);
        }else{
            System.arraycopy(uuid, 0, newUUID, 0, 16);
        }

        return newUUID;
    }

    /**
     * 0 - request
     * 1 - response
     */
    static boolean isRequest(byte[] msg){
        return msg[16] == 0;
    }

    /**
     * 0 - request
     * 1 - response
     */
    static boolean isResponse(byte[] msg){
        return msg[16] == 1;
    }

    static int getSeq(byte[] msg){
        return
        msg[17] << 24 |
        (msg[18] & 0xFF) << 16 |
        (msg[19] & 0xFF) << 8 |
        (msg[20] & 0xFF);
    }

    static byte[] createRequest(byte[] uuid, int seq, byte[] userData){
        return createMessage(uuid, true, seq, userData);
    }

    static byte[] createResponse(byte[] uuid, int seq, byte[] userData){
        return createMessage(uuid, false, seq, userData);
    }

    private static byte[] createMessage(byte[] uuid, boolean isRequest, int seq, byte[] userData){
        byte[] ret = new byte[21 + userData.length];
        System.arraycopy(uuid, 0, ret, 0, 16);
        ret[16] = (byte)(isRequest ? 0 : 1);
        ret[17] = (byte)(seq >>> 24);
        ret[18] = (byte)(seq >>> 16);
        ret[19] = (byte)(seq >>> 8);
        ret[20] = (byte)seq;
        System.arraycopy(userData, 0, ret, 21, userData.length);
        return ret;
    }


}
