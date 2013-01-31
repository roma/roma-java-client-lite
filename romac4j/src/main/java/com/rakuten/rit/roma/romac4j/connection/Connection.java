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
    private int bufferSize = 1024;

    public Connection(String nid, int bufferSize) {
        this.nodeId = nid;
        this.bufferSize = bufferSize;
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        super.connect(endpoint);
        is = new BufferedInputStream(getInputStream());
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

        byte[] sendCmd = null;
        if (value != null) {
            sendCmd = new byte[cmdBuff.length() + value.length + 2];
            System.arraycopy(cmdBuff.getBytes(), 0, sendCmd, 0, cmdBuff.length());
            System.arraycopy(value, 0, sendCmd, cmdBuff.length(), value.length);
            System.arraycopy("\r\n".getBytes(), 0, sendCmd, sendCmd.length - 2, 2);
        } else {
            sendCmd = cmdBuff.getBytes();
        }
        OutputStream os = new BufferedOutputStream(getOutputStream());
        os.write(sendCmd);
        os.flush();
    }

    public void write(String cmd) throws TimeoutException, IOException {
        write(cmd, null, null, null, -1);
    }

    public String getNodeId() {
        return nodeId;
    }

    public String readLine() throws IOException {
        byte[] b = new byte[1];
        byte[] buff = new byte[bufferSize];
        int i = 0;

        while (true) {
            if (i > bufferSize) {
                log.error("readLine() : Buffer overflow bufferSize=" + bufferSize + " i=" + i);
                throw new IOException("Too much receiveing data size.");
            }
            is.read(b, 0, 1);
            if (b[0] == 0x0d) {
                is.read(b, 0, 1);
                if (b[0] == 0x0a)
                    break;
            }
            buff[i] = b[0];
            i++;
        }
        return new String(buff, 0, i);
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