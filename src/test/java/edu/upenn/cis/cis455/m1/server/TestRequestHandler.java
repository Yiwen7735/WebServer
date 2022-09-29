package edu.upenn.cis.cis455.m1.server;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;


public class TestRequestHandler {
    
    WebService ws = new WebService();
    
    HttpWorkerPool workerPool = new HttpWorkerPool(0, null, ws) {
        @Override
        public Map<String, String> panel() {
            return new HashMap<>();
        }
    };
    
    Map<String, String> reqHeaders;
    
    @Before
    public void setUp() {
        ws.root = "/vagrant/555-hw1/www";
        ws.workerPool = workerPool;
        reqHeaders = new HashMap<>();
    }
    
    public void testHandleGETRequest() {
        Request req = new HttpRequest(
            "GET", "", "", "", "/", "HTTP/1.1", "", null, 45555, reqHeaders
        );
        
        RequestHandler handler = new StaticFileRequestHandler(ws);
        Response res = handler.handle(req);
        
        assertEquals(res.status(), 200);
        assertNotNull(res.body());
        assertEquals(res.type(), "text/html");
        assertEquals(res.length(), 202);
        assertEquals(res.responseLine(), "HTTP/1.1 200 OK\n");
    }
    
    @Test
    public void testHandleHEADRequest() {
        Request req = new HttpRequest(
            "HEAD", "", "", "", "/folder/pennEng.gif", "HTTP/1.1", "", null, 45555, reqHeaders
        );
        
        RequestHandler handler = new StaticFileRequestHandler(ws);
        Response res = handler.handle(req);
        
        assertEquals(res.status(), 200);
        assertNotNull(res.body());
        assertEquals(res.type(), "image/gif");
        assertEquals(res.length(), 13804);
    }
    
    @Test (expected = HaltException.class)
    public void testHandleAboveRootRequest() {
        Request req = new HttpRequest(
            "GET", "", "", "", "/../", "HTTP/1.1", "", null, 45555, reqHeaders
        );
        
        RequestHandler handler = new StaticFileRequestHandler(ws);
        Response res = handler.handle(req);
        assertNull(res);
    }
    
    @Test (expected = HaltException.class)
    public void testHandleGetNonExistentFileRequest() {
        Request req = new HttpRequest(
            "GET", "", "", "", "/third.html", "HTTP/1.1", "", null, 45555, reqHeaders
        );
        
        RequestHandler handler = new StaticFileRequestHandler(ws);
        Response res = handler.handle(req);
        assertNull(res);
    }
    

    @Test (expected = HaltException.class)
    public void testHandleIfModifiedSinceRequest() {
        reqHeaders.put("if-modified-since", "Saturday, 25-Sep-21 08:15:36 GMT");
        Request req = new HttpRequest(
            "HEAD", "", "", "", "/folder/pennEng.gif", "HTTP/1.1", "", null, 45555, reqHeaders
        );
        
        RequestHandler handler = new StaticFileRequestHandler(ws);
        Response res = handler.handle(req);
        assertNull(res);
    }
    
    @Test (expected = HaltException.class)
    public void testHandleIfUnmodifiedSinceRequest() {
        reqHeaders.put("if-unmodified-since", "Wed, 08-Sep-21 08:15:36 GMT");
        Request req = new HttpRequest(
            "HEAD", "", "", "", "/folder/pennEng.gif", "HTTP/1.1", "", null, 45555, reqHeaders
        );
        
        RequestHandler handler = new StaticFileRequestHandler(ws);
        Response res = handler.handle(req);
        assertNull(res);
    }
    
    
    @Test
    public void testHandleControlRequest() {
        Request req = new HttpRequest(
            "GET", "", "", "", "/control", "HTTP/1.1", "", null, 45555, null
        );
        RequestHandler handler = new ControlRequestHandler(ws);
        Response res = handler.handle(req);
        assertNotNull(res);
    }
}
