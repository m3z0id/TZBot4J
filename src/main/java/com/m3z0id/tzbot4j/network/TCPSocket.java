package com.m3z0id.tzbot4j.network;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TCPSocket extends DMCGenericSocket<Socket> {
    public TCPSocket(String hostname, int port) throws UnknownHostException, SocketException {
        super(hostname, port);
    }
    public TCPSocket(String hostname, int port, int bufferSize) throws UnknownHostException, SocketException {
        super(hostname, port, bufferSize);
    }

    @Override
    public void setTimeout(int millis) throws SocketException {
        sock.setSoTimeout(millis);
    }

    @Override
    protected void init() {
        this.sock = new Socket();
    }

    @Override
    public byte[] makeRequest(byte[] message) {
        try {
            send(message);
            return receive();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected byte[] receive() throws Exception {
        return sock.getInputStream().readNBytes(bufferSize);
    }

    @Override
    protected void send(byte[] message) throws Exception {
        sock.getOutputStream().write(message);
        sock.getOutputStream().flush();
    }

    @Override
    public void close() throws IOException {
        sock.close();
    }

    @Override
    public boolean isClosed() {
        return sock.isClosed();
    }
}
