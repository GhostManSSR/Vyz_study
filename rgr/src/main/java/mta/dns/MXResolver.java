package mta.dns;

import org.xbill.DNS.*;

public class MXResolver {

    public static String resolveMX(String domain) throws Exception {

        Lookup lookup = new Lookup(domain, Type.MX);
        org.xbill.DNS.Record[] records = lookup.run();

        if (records == null) {
            return domain;
        }

        MXRecord mx = (MXRecord) records[0];

        return mx.getTarget().toString();
    }
}
