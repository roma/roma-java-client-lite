package com.rakuten.rit.roma.romac4j.routing;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.rakuten.rit.roma.romac4j.Receiver;
import com.rakuten.rit.roma.romac4j.StringReceiver;
import com.rakuten.rit.roma.romac4j.connection.Connection;
import com.rakuten.rit.roma.romac4j.connection.RomaSocketPool;

public final class Routing extends Thread {
    protected static Logger log = Logger.getLogger(Routing.class.getName());
    private RomaSocketPool sps = RomaSocketPool.getInstance();
    private Random rnd = new Random(System.currentTimeMillis());
    private String[] initialNodes = null;
    private HashMap<String, Integer> failCountMap = new HashMap<String, Integer>();
    volatile private boolean threadLoop = false;
    volatile private RoutingData prevRoutingData = null;
    volatile private RoutingData routingData = null;

    private int failCount = 10;
    private int threadSleep = 5000;

    public Routing(String nodeId) {
        initialNodes = new String[] { nodeId };
    }

    public Routing(String[] nodes) {
        initialNodes = nodes;
    }

    public void run() {
        if (threadLoop) {
            log.warn("routing check thread : already running.");
            return;
        }
        log.info("routing check thread : started");
        threadLoop = true;
        while (threadLoop == true) {
            try {
                String myHash = null;
                String romaHash = getMklHash();
                if (romaHash != null) {
                    if (prevRoutingData != null) {
                        String prevHash = prevRoutingData.getMklHash("0");
                        if (romaHash.equals(prevHash)) {
                            Thread.sleep(threadSleep);
                            continue;
                        }
                    }
                    if (routingData != null) {
                        myHash = routingData.getMklHash("0");
                    }
                    if (!romaHash.equals(myHash)) {
                        RoutingData buf = getRoutingDump();
                        if (buf != null) {
                            prevRoutingData = null;
                            routingData = buf;
                            synchronized (failCountMap) {
                                failCountMap.clear();
                            }
                            log.info("routing check thread : Routing has changed.");
                        }
                    }
                }
                Thread.sleep(threadSleep);
            } catch (Exception e) {
                log.debug("routing check thread : ", e);
            }
        }
        log.info("routing check thread : stopped");
    }

    public void stopThread() {
        threadLoop = false;
    }

    public Connection getConnection() throws Exception {
        if (routingData != null) {
            return (sps.getConnection(routingData.getRandomNodeId()));
        } else {

            Connection c = null;
            String node = null;
            try {
                int n = rnd.nextInt(initialNodes.length);
                node = initialNodes[n];
                c =  sps.getConnection(node);
                getMklHash(c);
                return c;
            } catch(Exception e) {
                log.debug("getConnection error(" + node + ")", e);
                if (c != null) {
                    failCount(c);
                    c = null;
                }
            }

            // randam access node was dead. try to get connection sequentially.
            for(String n : initialNodes) {
                try {
                    c =  sps.getConnection(n);
                    getMklHash(c);
                    return c;
                } catch(Exception e) {
                    log.debug("getConnection error(" + n + ")", e);
                    if (c != null) {
                        failCount(c);
                        c = null;
                    }
                }
            }

            return null;
        }
    }

    public Connection getConnection(String key) throws Exception {
        String nid = null;
        try {
            nid = routingData.getPrimaryNodeId(key);
            if (nid == null) {
                log.error("getConnection() : can't get a primary node. key = "
                        + key);
                return getConnection();
            }
            return sps.getConnection(nid);
        } catch (NoSuchAlgorithmException ex) {
            log.error("getConnection() : ", ex);
            // fatal error : stop an application
            throw new RuntimeException("fatal : ", ex);
        } catch (Exception ex2) {
            log.error("getConnection() Exception. Return dummy connection object ", ex2);
            // returns a dummy connection for failCount()
            return new Connection(nid, 0);
        }
    }

    public void returnConnection(Connection con) {
        synchronized (failCountMap) {
            failCountMap.remove(con.getNodeId());
        }
        sps.returnConnection(con);
    }

    public void failCount(Connection con) {
        if(con == null){
            log.error("failCount(): got a null connection");
            return;
        }
        failCount(con.getNodeId());
        con.forceClose();
    }

    public void failCount(String nid) {
        int n = 0;
        synchronized (failCountMap) {
            if (failCountMap.containsKey(nid)) {
                n = failCountMap.get(nid);
            }
            n++;
            if (n >= failCount) {
                log.info("failCount(): failover");
                failCountMap.clear();
                prevRoutingData = routingData;
                if(routingData != null) {
                    routingData = routingData.failOver(nid);
                }
                sps.deleteConnection(nid);
            } else {
                failCountMap.put(nid, n);
            }
        }
    }

    public void setFailCount(int n) {
        failCount = n;
    }

    public void setThreadSleep(int n) {
        threadSleep = n;
    }

    private String getMklHash() {
        try {
            return getMklHash(getConnection());
        } catch (Exception e) {
            log.error("getMklHash() : ", e);
            return null;
        }
    }

    private String getMklHash(Connection con) {
        Receiver rcv = new StringReceiver();
        try {
            con.write("mklhash 0");
            rcv.receive(con);
            returnConnection(con);
        } catch (Exception e) {
            log.error("getMklHash() : ", e);
            failCount(con);
            return null;
        }
        return rcv.toString();
    }

    private RoutingData getRoutingDump() {
        Connection con = null;
        RoutingData routingData = null;
        Receiver rcv = new RoutingReceiver();
        try {
            con = getConnection();
            con.write("routingdump bin");
            rcv.receive(con);
            byte[] buff = ((RoutingReceiver) rcv).getValue();
            routingData = new RoutingData(buff);
            returnConnection(con);
        } catch (ParseException e) {
            log.error("getRoutingDump() : ", e);
            returnConnection(con);
            return null;
        } catch (Exception e) {
            log.warn("getRoutingDump() : ", e);
            failCount(con);
            return null;
        }
        return routingData;
    }

    private class RoutingReceiver extends Receiver {
        private byte[] value = null;

        @Override
        public void receive(Connection con) throws TimeoutException, IOException, ParseException {
            int len = 0;
            String str = con.readLine();
            if(str == null){
                log.error("RoutingReceiver.receive() : first line is null.");
                throw new IOException("first line is null.");
            }
            try {
                len = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                log.error("RoutingReceiver.receive() : NumberFormatException [" + str + "] ", e);
                throw new ParseException(str, -1);
            }
            if (len > 0) {
                value = con.readValue(len);
                con.readLine(); // "END\r\n"
            } else {
                value = new byte[0];
            }
        }

        public byte[] getValue() {
            return value;
        }
    }
}
