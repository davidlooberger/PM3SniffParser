package com.looberger.apdu.parser.util;

import com.github.devnied.emvnfccard.enums.CommandEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class APDUHelper {
    private static final Map<String, CommandEnum> lookup = new HashMap<String, CommandEnum>();
    static {
        for (CommandEnum c : CommandEnum.values()) {
            byte[] data =new byte[2];
            data[0]=(byte) c.getCla();
            data[1]=(byte) c.getIns();

            lookup.put(Util.byteArrayToHexString(data), c);
        }
 //       for (String key : lookup.keySet()) {
 //           System.out.println("Key in CommandMap: " +key + " Command: " + lookup.get(key));
 //       }
    }
    public static String prettyPrintAPDUCommand(byte[] data) {
        byte[] commandPart = Arrays.copyOfRange(data, 0, 2);
        CommandEnum ce = lookup.get(Util.byteArrayToHexString(commandPart));
        // Find and use length indicator
        String cmdData = "";
        String extraInfo = "";
        boolean asciiInterpret = showAsciiRepresentation(ce);
        if (ce.equals(CommandEnum.READ_RECORD)) {
            cmdData = Util.prettyPrintHex(Arrays.copyOfRange(data,2,data.length)) ;
        } else if (ce.equals(CommandEnum.GET_DATA)) {
            extraInfo = getExtraInfo(ce, data);
            cmdData = Util.prettyPrintHex(Arrays.copyOfRange(data,2,data.length)) ;
        } else if (data.length > 5) {
            int le = Util.byteToInt(data[5]);
            cmdData = Util.prettyPrintHex(Arrays.copyOfRange(data,4,data.length)) ;
        }
        if (ce != null) {
            return ce.toString() + " " + cmdData + extraInfo + (asciiInterpret ? " (" +new String(Util.fromHexString(cmdData)) +  ")" : "");
        } else {
            return "UNKNOWN_COMMAND " + "(" + Util.byteArrayToHexString(data) + ") " + (asciiInterpret ? " (" +new String(Util.fromHexString(cmdData)) +  ")" : "");
        }
    }

    private static String getExtraInfo(CommandEnum ce, byte[] data) {
        if (data[2] == (byte) 0x9F && data[3] == (byte) 0x5B) {
            return " DSDOL";
        } else if (data[2] == (byte)0x9F && data[3] == (byte)0x36) {
            return " (TRANSACTION COUNTER)";
        } else if (data[2] == (byte)0x9F && data[3] == (byte)0x4F) {
            return " (LOG FORMAT)";
        } else if (data[2] == (byte)0x9F && data[3] == (byte)0x17) {
            return " (LEFT PIN TRY)";
        } else {
            return "";
        }
    }

    private static boolean showAsciiRepresentation(CommandEnum ce) {
        boolean ret = false;
        switch (ce) {
            case SELECT:
               ret = true;
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }
}
