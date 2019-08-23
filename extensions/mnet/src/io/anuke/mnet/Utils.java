package io.anuke.mnet;

class Utils{


    public static String toString(byte[] a){
        return toString(a, a.length);
    }

    public static String toString(byte[] a, int len){
        if(a == null)
            return "null";
        int iMax = len - 1;
        if(iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        b.append(PacketType.toString(a[0])).append(", ");
        for(int i = 1; ; i++){
            b.append(a[i]);
            if(i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    public static byte[] trimDCMessage(String s, int bufferSize){
        if(s == null) return DCType.CLOSED.getBytes();
        byte[] msgBytes = s.getBytes();
        if(msgBytes.length <= bufferSize - 5){
            return msgBytes;
        }

        byte[] trimmed = new byte[bufferSize - 5];
        System.arraycopy(msgBytes, 0, trimmed, 0, trimmed.length);
        return trimmed;
    }

}
