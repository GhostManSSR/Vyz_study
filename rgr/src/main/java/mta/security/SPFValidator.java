package mta.security;

import org.xbill.DNS.*;

import java.lang.Record;

public class SPFValidator {

    public static boolean checkSPF(String domain) {

        try {

            Lookup lookup = new Lookup(domain, Type.TXT);

            org.xbill.DNS.Record[] records = lookup.run();

            if (records == null)
                return false;

            for (org.xbill.DNS.Record record : records) {

                TXTRecord txt = (TXTRecord) record;

                for (String value : txt.getStrings()) {

                    if (value.startsWith("v=spf1")) {
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
