package com.looberger.apdu.parser.util;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class DataEntry {
    long startime;
    long endtime;
    String source;
    String data;
    String crc;
    String info;

    public DataEntry(long startime, long endtime, String source, String data, String crc, String info) {
        this.startime = startime;
        this.endtime = endtime;
        this.source = source;
        this.data = data;
        this.crc = crc;
        this.info = info;
    }

    public DataEntry() {
        this.startime = 0;
        this.endtime = 0;
        this.source = null;
        this.data = "";
        this.crc = "";
        this.info = "";
    }

    public void appendData(String part) {
        if (StringUtils.isEmpty(data)) {
            data = part;
        } else {
            data += " " + part;
        }
    }

    public String strippedData() {
        String sData = data;
        if (sData!=null) {
         //   sData.replaceAll(" ","");
            if (sData.length() >= 2) {
                sData = sData.substring(2);
            }
           if (sData.length() > 5) {
                sData = sData.trim();
                sData = sData.substring(0,sData.length() - 6);
            }
        }
        return sData;
    }

    @Override
    public String toString() {
        return "DataEntry{" +
                "startime=" + startime +
                ", endtime=" + endtime +
                ", source='" + source + '\'' +
                ", data='" + data + '\'' +
                ", crc='" + crc + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}
