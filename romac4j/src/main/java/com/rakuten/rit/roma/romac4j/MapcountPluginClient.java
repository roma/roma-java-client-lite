package com.rakuten.rit.roma.romac4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class MapcountPluginClient extends ClientObject {
    protected static Logger log = Logger.getLogger(MapcountPluginClient.class
            .getName());

    public MapcountPluginClient(ClientObject obj) {
        super(obj);
    }

    public MapcountPluginClient(String nodeId) {
        super(nodeId);
    }

    public HashMap<String, Object> countup(String key,
            HashMap<String, Integer> keys) throws IOException {
        return countup(key, keys, 0);
    }

    public HashMap<String, Object> countup(String key,
            HashMap<String, Integer> keys, int expt) throws IOException {
        StringBuilder sb = new StringBuilder();
        Set<String> subKeys = keys.keySet();
        String subKey = null;
        for (Iterator<String> iter = subKeys.iterator(); iter.hasNext();) {
            subKey = iter.next();
            sb.append(subKey).append(":")
                    .append(Integer.toString((Integer)keys.get(subKey))).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return execute("mapcount_countup", key, sb, expt);
    }

    public HashMap<String, Object> update(String key, ArrayList<String> keys,
            int expt) throws IOException {
        return execute("mapcount_update", key, makeCommand(keys), expt);
    }

    public HashMap<String, Object> get(String key, ArrayList<String> keys)
            throws IOException {
        return execute("mapcount_get", key, makeCommand(keys), 0);
    }

    public HashMap<String, Object> get(String key)
            throws IOException {
        return execute("mapcount_get", key, makeCommand(null), 0);
    }

    private StringBuilder makeCommand(ArrayList<String> keys) {
        StringBuilder sb = new StringBuilder();
        if (keys != null && keys.size() > 0) {
            for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
                sb.append(iter.next()).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb;
    }

    private HashMap<String, Object> execute(String comm, String key,
            StringBuilder sb, int expt) throws IOException {
        ValueReceiver rcv = sendCmdV(comm, key, expt + " " + sb.length(), sb
                .toString().getBytes());
        String value = rcv.getValueString();
        if (value == null) {
          return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        HashMap<String, Object> result = objectMapper.readValue(
                value, HashMap.class);
        return result;
    }
}
