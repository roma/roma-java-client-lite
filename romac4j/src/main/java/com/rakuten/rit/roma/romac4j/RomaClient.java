package com.rakuten.rit.roma.romac4j;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class RomaClient extends ClientObject {
    protected static Logger log = Logger.getLogger(RomaClient.class.getName());

    public RomaClient(ClientObject obj) {
        super(obj);
    }

    public RomaClient(String nodeId) {
        super(nodeId);
    }

    public RomaClient(String[] nodeIds) {
        super(nodeIds);
    }

    public byte[] get(String key) throws IOException {
        return sendCmdV("get", key).getValue();
    }

    public String getString(String key) throws IOException {
        return sendCmdV("get", key).getValueString();
    }

    private ValueReceiver sendGetsCommand(String[] keys) throws IOException {
        if(keys == null || keys.length == 0) return null;
        if(keys.length == 1){
            return sendCmdV("get", keys[0]);
        }
        String key = keys[0];
        String opt = keys[1];
        for(int i = 2; i < keys.length; i++){
            if(keys[i].contains(" ")){
                throw new IllegalArgumentException("Can't include space in a key.");
            }
            opt += " " + keys[i];
        }
        return sendCmdV("gets", key, opt);
    }
    
    public Map<String, byte[]> gets(String[] keys) throws IOException {
        ValueReceiver rcv = sendGetsCommand(keys);
        if(rcv == null) return null;
        
        int len = rcv.size();
        HashMap<String, byte[]> ret = new HashMap<String, byte[]>();
        for(int i = 0; i < len; i++){
            String key = rcv.getHeader(i).split(" ")[1];    // VALUE <key> <bytes> [<cas unique>]\r\n
            ret.put(key, rcv.getValue(i));                  // <data block>\r\n
        }
        return ret;
    }
    
    public Map<String, String> getsString(String[] keys) throws IOException {
        ValueReceiver rcv = sendGetsCommand(keys);
        if(rcv == null) return null;
        
        int len = rcv.size();
        HashMap<String, String> ret = new HashMap<String, String>();
        for(int i = 0; i < len; i++){
            String key = rcv.getHeader(i).split(" ")[1];    // VALUE <key> <bytes> [<cas unique>]\r\n
            ret.put(key, rcv.getValueString(i));            // <data block>\r\n
        }
        return ret;
    }
    
    private boolean set(String cmd, String key, byte[] value, int expt)
            throws IOException {
        return sendCmdS(cmd, key, "0 " + expt + " " + value.length, value).isStored();
    }

    public boolean set(String key, byte[] value, int expt)
            throws IOException {
        return set("set", key, value, expt);
    }
    
    public boolean set(String key, String value, int expt)
            throws IOException {
        return set("set", key, value.getBytes(), expt);        
    }

    public boolean add(String key, byte[] value, int expt)
            throws IOException {
        return set("add", key, value, expt);
    }

    public boolean add(String key, String value, int expt)
            throws IOException {
        return set("add", key, value.getBytes(), expt);
    }

    public boolean replace(String key, byte[] value, int expt)
            throws IOException {
        return set("replace", key, value, expt);
    }

    public boolean replace(String key, String value, int expt)
            throws IOException {
        return set("replace", key, value.getBytes(), expt);
    }

    public boolean append(String key, byte[] value, int expt)
            throws IOException {
        return set("append", key, value, expt);
    }

    public boolean append(String key, String value, int expt)
            throws IOException {
        return set("append", key, value.getBytes(), expt);
    }

    public boolean prepend(String key, byte[] value, int expt)
            throws IOException {
        return set("prepend", key, value, expt);
    }

    public boolean prepend(String key, String value, int expt)
            throws IOException {
        return set("prepend", key, value.getBytes(), expt);
    }

    public Long incr(String key, long value) throws IOException {
        try{
            return Long.parseLong(sendCmdS("incr", key, "" + value).toString());
        }catch(NumberFormatException e){
            return null;
        }
    }
    
    public BigInteger incrBigInt(String key, long value) throws IOException {
        try{
            return new BigInteger(sendCmdS("incr", key, "" + value).toString());
        }catch(NumberFormatException e){
            return null;
        }
    }

    public Long decr(String key, long value) throws IOException {
        try{
            return Long.parseLong(sendCmdS("decr", key, "" + value).toString());
        }catch(NumberFormatException e){
            return null;
        }
    }

    public BigInteger decrBigInt(String key, long value) throws IOException {
        try{
            return new BigInteger(sendCmdS("decr", key, "" + value).toString());
        }catch(NumberFormatException e){
            return null;
        }
    }

    public boolean delete(String key) throws IOException {
        return sendCmdS("delete", key).isDeleted();
    }

    public boolean setExpt(String key, int expt) throws IOException {
        return sendCmdS("set_expt", key, "" + expt).isStored();
    }

    public boolean cas(String key, int expt, Cas callback) throws IOException {
        ValueReceiver rcv = sendCmdV("gets", key);
        if(rcv.getValue() == null) return false;
        int casid = 0;
        try {
            casid = ((ValueReceiver) rcv).getCasid();
        } catch (ParseException e) {
            log.error("cas() : " + e.getMessage());
            return false;
        }
        byte[] value = callback.cas(rcv);

        return sendCmdS("cas", key,
                "0 " + expt + " " + value.length, value, casid).isStored();
    }
}
