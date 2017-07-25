package com.rakuten.rit.roma.romac4j;

import java.io.IOException;
import java.text.ParseException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.rakuten.rit.roma.romac4j.connection.Connection;
import com.rakuten.rit.roma.romac4j.connection.RomaSocketPool;
import com.rakuten.rit.roma.romac4j.routing.Routing;

public class ClientObject {
    protected static Logger log = Logger.getLogger(ClientObject.class.getName());
    protected Routing routing = null;
    protected int maxRetry = 5;

    public ClientObject(ClientObject obj){
        this.routing = obj.routing;
        this.maxRetry = obj.maxRetry;
    }

    public ClientObject(String nodeId) {
        this(new String[]{ nodeId} );
    }

    public ClientObject(String[] nodeIds) {
        BasicConfigurator.configure();

        try{
            RomaSocketPool.getInstance();
        } catch(RuntimeException e){
            RomaSocketPool.init();
            log.warn("ClientObject() : SocketPool initialized in RomaClient().");
        }

        routing = new Routing(nodeIds);
        routing.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("ClientObject() : " + e.getMessage());
        }
    }

    public void destroy() {
        routing.stopThread();
    }

    public void setMaxRetry(int n) {
        maxRetry = n;
    }
    
    public void setFailCount(int n) {
        routing.setFailCount(n);
    }
    
    public void setThreadSleep(int n) {
        routing.setThreadSleep(n);
    }

    protected StringReceiver sendCmdS(String cmd, String key)
            throws IOException {
        return (StringReceiver)sendCmd(new StringReceiver(), cmd, key, null, null, -1);
    }

    protected StringReceiver sendCmdS(String cmd, String key, String opt)
            throws IOException {
        return (StringReceiver)sendCmd(new StringReceiver(), cmd, key, opt, null, -1);
    }

    protected StringReceiver sendCmdS(String cmd, String key, String opt, byte[] value)
            throws IOException {
        return (StringReceiver)sendCmd(new StringReceiver(), cmd, key, opt, value, -1);
    }

    protected StringReceiver sendCmdS(String cmd, String key, String opt, byte[] value, int casid)
            throws IOException {
        return (StringReceiver)sendCmd(new StringReceiver(), cmd, key, opt, value, casid);
    }

    protected ValueReceiver sendCmdV(String cmd, String key)
            throws IOException {
        return (ValueReceiver)sendCmd(new ValueReceiver(), cmd, key, null, null, -1);
    }

    protected ValueReceiver sendCmdV(String cmd, String key, String opt)
            throws IOException {
        return (ValueReceiver)sendCmd(new ValueReceiver(), cmd, key, opt, null, -1);
    }

    protected ValueReceiver sendCmdV(String cmd, String key, String opt, byte[] value)
            throws IOException {
        return (ValueReceiver)sendCmd(new ValueReceiver(), cmd, key, opt, value, -1);
    }

    protected Receiver sendCmd(Receiver rcv, String cmd, String key,
            String opt, byte[] value, int casid) throws IOException {
        if(key.contains(" ")){
            throw new IllegalArgumentException("Can't include space in a key.");
        }
        boolean retry;
        do {
            retry = false;
            Connection con = null;
            try {
                con = routing.getConnection(key);
                con.write(cmd, key, opt, value, casid);
                rcv.receive(con);
                routing.returnConnection(con);
            } catch (ParseException e) {
                routing.returnConnection(con);
                log.error("sendCmd(): " + e.getMessage());
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.error("sendCmd(): " + e.getMessage());
                retry = true;
                log.debug("sendCmd(): retry=" + rcv.retry);
                routing.failCount(con);
                if (++rcv.retry >= maxRetry) {
                    log.error("sendCmd(): Retry out");
                    throw new IOException("Retry out", e);
                }
            }
        } while (retry);

        return rcv;
    }
    
}
