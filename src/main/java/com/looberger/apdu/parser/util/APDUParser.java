package com.looberger.apdu.parser.util;

import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.looberger.apdu.parser.ParserApplication;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class APDUParser {
    private static Logger LOG = LoggerFactory.getLogger(APDUParser.class);

    private enum SOURCE {
        Card("Tag"),
        Reader("Reader");

        private String value;

        SOURCE(String tag) {
            value = tag;
        }
    }

    List<String> dataLines = new ArrayList<>();
    List<DataEntry> dataEntries = new ArrayList<>();
    DataEntry current = null;
    boolean started = false;
    boolean ended = false;

    public void collect(String line) {
        if (!started && line.contains("Start") && line.contains("End") && line.contains("Src")) { // Locate start
            started = true;
        } else if (line.startsWith("--------")) { // Skip divider
            ;
        } else if (started) {
            try {

                List<String> parts = Pattern.compile("\\|")
                        .splitAsStream(line)
                        .map(String::trim)
                        .collect(Collectors.toList());
                if (current == null) {
                    current = new DataEntry();
                } else if (!parts.get(2).isEmpty()) {
                    dataEntries.add(current);
                    current = new DataEntry();
                }
                int loopCount = 0;
                for (String part : parts) {
                    if (loopCount == 0 && !part.isEmpty()) {
                        current.setStartime(Long.parseLong(part));
                    }
                    if (loopCount == 1 && !part.isEmpty()) {
                        current.setEndtime(Long.parseLong(part));
                    }
                    if (loopCount == 2 && !part.isEmpty()) {
                        current.setSource(part);
                    }
                    if (loopCount == 3 && !part.isEmpty()) {
                        current.appendData(part);
                    }
                    if (loopCount == 4 && !part.isEmpty()) {
                        current.setCrc(part);
                    }
                    if (loopCount == 5 && !part.isEmpty()) {
                        current.setInfo(part);
                    }
                    loopCount++;
                }
                // Store, and create new after CRC is received
                if (StringUtils.isNotEmpty(current.getCrc())) {
                    dataEntries.add(current);
                    current = null;
                }
            } catch (Exception e) {
                LOG.error("Caught " + e.getMessage(), e);
            }
        }
    }

    public void dump() {
        for (DataEntry dataEntry : dataEntries) {
            try {
                // Skip data we are not interested in...
                if (dataEntry.data.startsWith("02") || dataEntry.data.startsWith("03")) {
                    System.out.println("\nStart " + dataEntry.startime);
                    if (dataEntry.source.equals("Tag")) {
                        System.out.println("Card/Tag ==>: \n    (Raw) " + dataEntry.data);
                        System.out.println("    " + TlvUtil.prettyPrintAPDUResponse(Util.fromHexString(dataEntry.strippedData())));
                    } else {
                        System.out.println("Reader ==> \n    (Raw) " + dataEntry.data);
                        System.out.println("    " + APDUHelper.prettyPrintAPDUCommand(Util.fromHexString(dataEntry.strippedData())));
                    }
                    System.out.println("Endtime " + dataEntry.endtime + " CRC " + dataEntry.crc + " Info " + dataEntry.info);
                } else {
                    ;
                }
            } catch (Exception e) {
                //LOG.error("Exception ",e);
                System.out.println("Endtime " + dataEntry.endtime + " CRC " + dataEntry.crc + " Info " + dataEntry.info);
            }
            //System.out.println(dataEntry.toString());
        }
    }
}
