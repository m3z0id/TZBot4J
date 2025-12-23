package com.m3z0id.tzbot4j.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public abstract class DMCGenericSocket<T extends Closeable> {
    protected T sock;
    protected String hostname;
    protected InetAddress addr;
    protected int port;
    protected int bufferSize = 16384; // Default buffer size

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public String getHostname() {
        return hostname;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    public int getBufferSize() {
        return bufferSize;
    }

    public DMCGenericSocket(String hostname, int port) throws UnknownHostException, SocketException {
        this.hostname = hostname;
        this.port = port;
        this.addr = InetAddress.getByName(hostname);
        init();
    }

    public DMCGenericSocket(String hostname, int port, int bufferSize) throws UnknownHostException, SocketException {
        this.hostname = hostname;
        this.port = port;
        this.bufferSize = bufferSize;
        this.addr = InetAddress.getByName(hostname);
        init();
    }

    public abstract void setTimeout(int millis) throws SocketException;
    protected abstract void init() throws SocketException;

    public abstract byte[] makeRequest(byte[] message);

    protected abstract byte[] receive() throws Exception;
    protected abstract void send(byte[] message) throws Exception;

    public abstract void close() throws IOException;
    public abstract boolean isClosed();
}



