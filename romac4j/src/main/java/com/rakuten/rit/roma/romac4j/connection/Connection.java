package com.rakuten.rit.roma.romac4j.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

public class Connection extends Socket {
    protected static Logger log = Logger.getLogger(Connection.class.getName());
    private String nodeId = null;
    private InputStream is = null;
    private OutputStream os = null;
    private int bufferSize = 1024;

    public Connection(String nid, int bufferSize) {
        this.nodeId = nid;
        this.bufferSize = bufferSize;
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        super.connect(endpoint, timeout);
        is = new BufferedInputStream(getInputStream());
        os = new BufferedOutputStream(getOutputStream());
    }
    
    public void write(String cmd, String key, String opt, byte[] value,
            int casid) throws TimeoutException, IOException {
        if (cmd == null || cmd.length() == 0) {
            log.error("write() : cmd string is null or empty.");
            // fatal error : stop an application
            throw new IllegalArgumentException("fatal : cmd string is null or empty.");
        }
        String cmdBuff = cmd;
        if (key != null && key.length() != 0) {
            cmdBuff += " " + key;
        }

        if (opt != null && opt.length() != 0) {
            cmdBuff += " " + opt;
        }

        if (casid != -1) {
            cmdBuff += " " + casid;
        }
        cmdBuff += "\r\n";

        os.write(cmdBuff.getBytes());
        if (value != null) {
            os.write(value);
            os.write("\r\n".getBytes());
        }
        os.flush();
    }

    public void write(String cmd) throws TimeoutException, IOException {
        write(cmd, null, null, null, -1);
    }

    public String getNodeId() {
        return nodeId;
    }

    public String readLine() throws IOException {
        byte[] buff = new byte[bufferSize];
        int b, i = 0;

        while (i < bufferSize) {
            if ((b = is.read()) == 0x0d) {
                if ((b = is.read()) == 0x0a)
                    return new String(buff, 0, i);
            }
            buff[i++] = (byte)b;
        }
        log.error("readLine() : Buffer overflow bufferSize=" + bufferSize + " i=" + i);
        throw new IOException("Too much receiveing data size.");
    }

    public byte[] read(int n) throws IOException {
        byte[] ret = new byte[n];
        int off = 0, cnt = 0;
        
        while ((n -= cnt) > 0) {
            cnt = is.read(ret, off, n);
            off += cnt;
        }
        return ret;
    }
    
    public byte[] readValue(int n) throws IOException {
        byte[] result = read(n);
        read(2); // "\r\n"
        return result;
    }

    public void forceClose() {
        try {
            if (!this.isClosed())
                this.close();
        } catch (IOException e) {
            log.warn("forceClose() : " + e.getMessage());
        }
    }
}
