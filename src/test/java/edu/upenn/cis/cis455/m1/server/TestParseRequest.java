package edu.upenn.cis.cis455.m1.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m1.interfaces.Request;

import org.apache.logging.log4j.Level;

public class TestParseRequest {
    
    String sampleHeaders = 
        "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
        "Host: www.cis.upenn.edu\r\n" +
        "Accept-Language: en-us\r\n" +
        "Accept-Encoding: gzip, deflate\r\n" +
        "Cookie: name1=value1; name2=value2; name3=value3\r\n" +
        "Connection: Keep-Alive\r\n\r\n";
    
    Socket socket = mock(Socket.class);
 
    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }
    
    public void setUpSocket(byte[] input, InetSocketAddress ip, int port) {
        final ByteArrayInputStream in = new ByteArrayInputStream(input);
        try {
            when(socket.getInputStream()).thenReturn(in);
            when(socket.getRemoteSocketAddress()).thenReturn(ip);
            when(socket.getLocalPort()).thenReturn(port);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void setUpSocket(byte[] input) {
        setUpSocket(input, InetSocketAddress.createUnresolved("host", 8080), 80);
    }
    
    @Test (expected = HaltException.class)
    public void testEmptyRequest() {
        setUpSocket("".getBytes());
        HttpIoHandler.parseRequest(socket);
    }
    
    @Test (expected = HaltException.class)
    public void testMissingUrlOfRequestLine() {
        setUpSocket(("GET\r\n" + sampleHeaders).getBytes());
        HttpIoHandler.parseRequest(socket);
    }
    
    @Test (expected = HaltException.class)
    public void testMissingProtocolOfRequestLine() {
        setUpSocket(("GET /a/b/hello.htm?q=x&v=12%200\r\n" + sampleHeaders).getBytes());
        HttpIoHandler.parseRequest(socket);
    }
    
    @Test (expected = HaltException.class)
    public void testLowerCaseProtocol() {
        setUpSocket("GET /a/b/hello.htm?q=x&v=12%200 http/1.0\r\n".getBytes());
        HttpIoHandler.parseRequest(socket);
    }

    @Test (expected = HaltException.class)
    public void testMissingHost() {
        setUpSocket("GET /a/b/hello.htm?q=x&v=12%200 HTTP/1.1\r\n".getBytes());
        HttpIoHandler.parseRequest(socket);
    }
    
    @Test
    public void testCorrectGETRequest() {
        String line = "GET /a/b/hello.htm?q=x&v=12%200 HTTP/1.1\r\n";
        InetSocketAddress clientIP = InetSocketAddress.createUnresolved("host", 8080);
        
        byte[] arr = (line + sampleHeaders).getBytes();
        setUpSocket(arr, clientIP, 80);
        Request req = HttpIoHandler.parseRequest(socket);
        
        assertEquals(req.requestMethod(), "GET");
        assertEquals(req.host(), "www.cis.upenn.edu");
        assertEquals(req.userAgent(), "Mozilla/4.0 (compatible; MSIE5.01; Windows NT)");
        assertEquals(req.port(), 80);
        assertEquals(req.url(), "http://www.cis.upenn.edu/a/b/hello.htm?q=x&v=12%200");
        assertEquals(req.uri(), "http://www.cis.upenn.edu/a/b/hello.htm");
        assertEquals(req.pathInfo(), "/a/b/hello.htm");
        assertEquals(req.protocol(), "HTTP/1.1");
        assertEquals(req.contentType(), null);
        assertEquals(req.contentLength(), 0);
        assertEquals(req.ip(), clientIP.toString());
        assertEquals(req.body(), null);
        assertFalse(req.persistentConnection());
        assertEquals(req.headers("accept-language"), "en-us");
        assertEquals(req.headers("Accept-Encoding"), "gzip, deflate");
        assertEquals(req.headers().size(), 6 + 2); // Add method and protocol to headers
    }
    
    @Test
    public void testCorrectHEADRequest() {
        String newHeaders = 
            "Host: www.cIS555:45555\r\n" +
            "Cookie: name1=value1; name2=value2; name3=value3\r\n";
        
        String line = "HEAD /hello/world HTTP/1.1\r\n";
        InetSocketAddress clientIP = InetSocketAddress.createUnresolved("host", 8080);
        
        byte[] arr = (line + newHeaders).getBytes();
        setUpSocket(arr, clientIP, 45555);
        Request req = HttpIoHandler.parseRequest(socket);
        
        assertEquals(req.requestMethod(), "HEAD");
        assertEquals(req.host(), "www.cis555:45555"); // transfered to lower case
        assertEquals(req.userAgent(), null);
        assertEquals(req.port(), 45555);
        assertEquals(req.url(), "http://www.cis555:45555/hello/world"); //lower case
        assertEquals(req.uri(), "http://www.cis555:45555/hello/world");
        assertEquals(req.pathInfo(), "/hello/world");
        assertEquals(req.protocol(), "HTTP/1.1");
        assertEquals(req.contentType(), null);
        assertEquals(req.contentLength(), 0);
        assertEquals(req.ip(), clientIP.toString());
        assertEquals(req.body(), null);
        assertFalse(req.persistentConnection());
        assertEquals(req.headers().size(), 2 + 2); // Add method and protocol to headers
    }

    @Test
    public void testAbsoluteUrl() {
        String newHeaders = 
                "Cookie: name1=value1; name2=value2; name3=value3\r\n";
        
        String line = "HEAD http://localhost:45555/hello/world HTTP/1.0\r\n";
        InetSocketAddress clientIP = InetSocketAddress.createUnresolved("host", 8080);
        setUpSocket((line + newHeaders).getBytes(), clientIP, 45555);
        
        Request req = HttpIoHandler.parseRequest(socket);
        
        assertEquals(req.requestMethod(), "HEAD");
        assertEquals(req.host(), "localhost:45555");
        assertEquals(req.userAgent(), null);
        assertEquals(req.port(), 45555);
        assertEquals(req.url(), "http://localhost:45555/hello/world");
        assertEquals(req.uri(), "http://localhost:45555/hello/world");
        assertEquals(req.pathInfo(), "/hello/world");
        assertEquals(req.protocol(), "HTTP/1.0");
        assertEquals(req.contentType(), null);
        assertEquals(req.contentLength(), 0);
        assertEquals(req.ip(), clientIP.toString());
        assertEquals(req.body(), null);
        
        assertFalse(req.persistentConnection());
        req.persistentConnection(true);
        assertTrue(req.persistentConnection());
        
        assertEquals(req.headers().size(), 1 + 2); // Add method and protocol to headers
        
    }
    
    
}




