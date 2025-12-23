package com.m3z0id.tzbot4j.network;

import java.net.*;
import java.util.Arrays;

public class UDPSocket extends DMCGenericSocket<DatagramSocket> {
    public UDPSocket(String hostname, int port) throws SocketException, UnknownHostException {
        super(hostname, port);
    }
    public UDPSocket(String hostname, int port, int bufferSize) throws SocketException, UnknownHostException {
        super(hostname, port, bufferSize);
    }

    protected void init() throws SocketException {
        this.sock = new DatagramSocket();
        this.sock.setSoTimeout(3000);
        this.sock.setReuseAddress(true);
    }

    public byte[] makeRequest(byte[] message) {
        try {
            send(message);
            return receive();
        } catch (Exception e) {
            return null;
        }
    }

    protected void send(byte[] message) throws Exception {
        InetAddress address = InetAddress.getByName(hostname);
        DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
        sock.send(packet);
    }

    protected byte[] receive() throws Exception {
        byte[] buffer = new byte[this.bufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        sock.receive(packet);
        return Arrays.copyOfRange(buffer, 0, packet.getLength());
    }

    public void setTimeout(int millis) throws SocketException {
        sock.setSoTimeout((int) millis);
    }


    public void close() {
        sock.close();
    }

    public boolean isClosed() {
        return sock.isClosed();
    }
}
