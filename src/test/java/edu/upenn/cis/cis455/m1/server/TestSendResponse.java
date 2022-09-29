package edu.upenn.cis.cis455.m1.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

import org.apache.logging.log4j.Level;

public class TestSendResponse {

    Socket socket;
    ByteArrayOutputStream out;
    
    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }
    
    String sampleGetRequest = 
        "GET /a/b/hello.htm?q=x&v=12%200 HTTP/1.1\r\n" +
        "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
        "Host: www.cis.upenn.edu\r\n" +
        "Accept-Language: en-us\r\n" +
        "Accept-Encoding: gzip, deflate\r\n" +
        "Cookie: name1=value1; name2=value2; name3=value3\r\n" +
        "Connection: Keep-Alive\r\n\r\n";
    
    @Test
    public void testSendResponse() throws IOException {
        out = new ByteArrayOutputStream();
        socket = TestHelper.getMockSocket(sampleGetRequest, out);
        
        String body = 
            "<!DOCTYPE html>" + 
            "<html>" + 
                "<head>" + 
                    "<title>Hello world</title>" +
                "</head>" + 
            "</html>";
        
        Request req = new HttpRequest("GET", "", "", "", "", "", "", null, 45555, null);
        Response res = new HttpResponse();
        res.type("text/html");
        res.body(body);
        
        HttpIoHandler.sendResponse(socket, req, res);
        
        String result = out.toString("UTF-8").replace("\r", "");
        
        assertTrue(result.startsWith("HTTP/1.1 200 OK"));
        assertTrue(result.contains("Connection: close"));
        assertTrue(result.contains("\n\n" + body));
    }
    
    @Test
    public void testSend100ContinueResponse() throws IOException {
        out = new ByteArrayOutputStream();
        socket = TestHelper.getMockSocket(sampleGetRequest, out);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("expect", "100-continue");
        Request req = new HttpRequest(
            "HEAD", "", "", "", "/", "HTTP/1.1", "", null, 45555, headers
        );
        
        Response res = new HttpResponse();
        res.status(100);
        
        HttpIoHandler.sendResponse(socket, req, res);
        String result = out.toString("UTF-8").replace("\r", "");
        
        assertTrue(result.startsWith("HTTP/1.1 100 Continue"));
        
    }
}
