package moe.nightfall.vic.chat.util;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Bridge to avoid JDK-8077109 issue with Java 8u51 and later
 * This SocketFactory ensure that ALL ciphers are enabled.
 */
public class SSLSocketFactoryBridge extends SSLSocketFactory
{
    private final SSLSocketFactory bridge;

    public SSLSocketFactoryBridge(SSLSocketFactory bridge)
    {
        this.bridge = bridge;
    }

    @Override
    public String[] getDefaultCipherSuites()
    {
        return bridge.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites()
    {
        return bridge.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException
    {
        SSLSocket result = (SSLSocket) bridge.createSocket(socket, s, i, b);
        result.setEnabledCipherSuites(getSupportedCipherSuites());
        return result;
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException, UnknownHostException
    {
        SSLSocket socket = (SSLSocket) bridge.createSocket(s, i);
        socket.setEnabledCipherSuites(getSupportedCipherSuites());
        return socket;
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException
    {
        SSLSocket socket = (SSLSocket) bridge.createSocket(s, i, inetAddress, i1);
        socket.setEnabledCipherSuites(getSupportedCipherSuites());
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException
    {
        SSLSocket socket = (SSLSocket) bridge.createSocket(inetAddress, i);
        socket.setEnabledCipherSuites(getSupportedCipherSuites());
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException
    {
        SSLSocket socket = (SSLSocket)bridge.createSocket(inetAddress, i, inetAddress1, i1);;
        socket.setEnabledCipherSuites(getSupportedCipherSuites());
        return socket;
    }
}
