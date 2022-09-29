package edu.upenn.cis.cis455.m1.handling;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.exceptions.ClosedConnectionException;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;
import edu.upenn.cis.cis455.m1.server.HttpParsing;
import edu.upenn.cis.cis455.m1.server.HttpRequest;
import edu.upenn.cis.cis455.m1.server.HttpResponse;

/**
 * Handles marshaling between HTTP Requests and Responses
 */ 
public class HttpIoHandler {
    final static Logger logger = LogManager.getLogger(HttpIoHandler.class);

    protected static final String HOST = "localhost";
    protected static final String SERVER = "CIS-555"; // Server name

    public static Request parseRequest(Socket socket) throws ClosedConnectionException, HaltException {

        String ip = socket.getRemoteSocketAddress().toString(); // Client's IP address
        int port = socket.getLocalPort();  // Our server port

        Map<String, String> headers = new HashMap<>();
        Map<String, List<String>> parms = new HashMap<>();
        InputStream in = null;
        
        try {
            in = socket.getInputStream(); 
        } catch (IOException e) {
            throw new ClosedConnectionException();
        }

        String pathQuery = "";
        // Populate headers and params, reuturn a relative path, e.g., /compute/add?x=1&y=2
        // However, HttpParsing.parseRequest could throw two types of HaltException (400, 500)
        // along with ClosedConnectionException (some in the form of IOException)
        try {
            pathQuery = HttpParsing.parseRequest(ip, in, headers, parms);
        } catch (ClosedConnectionException | IOException e) {
            throw new ClosedConnectionException();
        } catch (HaltException e) {
            throw new HaltException(e.statusCode(), e.body());
        }

        // Use our host if not provided by client
        String host = headers.getOrDefault("host", HOST + ":" + port).toLowerCase();

        //url: http://localhost:45555/compute/add?x=1&y=2
        //uri: http://localhost:45555/compute/add
        //pathInfo: /compute/add
        String url, uri, pathInfo; 
        int absPathIndex = pathQuery.indexOf("//");

        if (absPathIndex == -1) { // relative path provided,e,g, /compute/add?x=1&y=2
            url = "http://" + host + pathQuery;   
            pathInfo = pathQuery;
        } else {                             // absolute path provided 
            url = pathQuery; 
            int relPathIndex = pathQuery.indexOf('/', absPathIndex + 2);
            host = pathQuery.substring(absPathIndex + 2, relPathIndex);
            pathInfo = url.substring(relPathIndex);
        }

        uri = url;

        // Need to cut off query string from uri and pathInfo
        int queryIndex;
        if ((queryIndex = uri.indexOf('?')) != -1) {
            uri = uri.substring(0, queryIndex);
        }

        if ((queryIndex = pathInfo.indexOf('?')) != -1) {
            pathInfo = pathInfo.substring(0, queryIndex);
        }

        logger.debug("original, url, uri & pathInfo: " + pathQuery + ", " + url + ", " + uri + ", " + pathInfo);

        String method = headers.get("method");
        String protocol = headers.get("protocolVersion");

        return new HttpRequest(method, host, url, uri, pathInfo, protocol, ip, null, port, headers);
    }

    /**
     * Sends an exception back, in the form of an HTTP response code and message.
     * Returns true if we are supposed to keep the connection open (for persistent
     * connections).
     */
    public static boolean sendException(Socket socket, Request request, HaltException except) {
        HttpResponse response = new HttpResponse();
        int statusCode = except.statusCode();
        response.status(statusCode);

        StringBuilder sb = new StringBuilder(response.responseLine());
        System.out.println(sb.toString());
        sb.append("Date: " + DateUtils.getCurrentTime() + "\n");
        if (request == null || !request.persistentConnection()) {
            sb.append("Connection: close\n");
        }
        sb.append('\n'); // End with an additional newline
        
        try {
            OutputStream out = socket.getOutputStream();
            out.write(sb.toString().getBytes());
            out.flush();
        } catch (IOException e) {
        	logger.warn("IOException thrown while sending response back.");
        }
        return false; 
    }

    /**
     * Sends data back. Returns true if we are supposed to keep the connection open
     * (for persistent connections).
     */
    public static boolean sendResponse(Socket socket, Request request, Response response) {
        if (request == null) {
            logger.error("Request is null in sendResponse");
            return false;
        }
        response.server(SERVER);

        StringBuilder sb = new StringBuilder(response.responseLine());
        if (response.status() > 100) {
            sb.append(response.getHeaders());
            if (!request.persistentConnection()) {
                sb.append("Connection: close\n");
            }
        }
        sb.append('\n'); // Separate header and body with an additional newline

        try {
        	OutputStream out = socket.getOutputStream();
        	out.write(sb.toString().getBytes());
        	if (response.status() > 100 && !request.requestMethod().equals("HEAD")) {
                out.write(response.bodyRaw());
            }
        	out.flush();
        } catch (IOException e) {
            logger.debug(e.getStackTrace());
        	logger.warn("IOException thrown while sending response back.");
        }
        return false;
    }
}
