package edu.upenn.cis.cis455.m1.server;

import java.io.IOException;

import java.net.Socket;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.exceptions.ClosedConnectionException;
import edu.upenn.cis.cis455.exceptions.HaltException;

import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

/**
 * Stub class for a thread worker that handles Web requests
 */
public class HttpWorker implements Runnable {

    static final Logger logger = LogManager.getLogger(HttpWorker.class);

    // Flag to indicate when to stop
    protected boolean stop = false;
    protected volatile String task = null;
    protected HttpTaskQueue taskQueue;
    protected WebService webService;

    public HttpWorker(HttpTaskQueue queue, WebService webService) {
        taskQueue = queue;
        this.webService = webService;
    }
 
    
    public void stop() { 
        stop = true;
    }

    public void task(String task) {
        this.task = task;
    }

    @Override
    public void run() {
        while (!stop) { 
            try {
                // Obtain the socket originally passed by HttpListener
                HttpTask task = taskQueue.dequeue();
                Socket socket = task.getSocket();
                
                // In case the thread is blocked waiting for input from client
                // Wait for 60 seconds for one request
                socket.setSoTimeout(60000);
                
                // HttpIoHander could throw ClosedConnection/Halt Exception
                // In both cases, close the socket and move onto the next task
                // DO NOT forget to sendException in case of halt exception
                Request request = null;
                try {
                    request = HttpIoHandler.parseRequest(socket); 
                } catch (ClosedConnectionException e) {
                	logger.debug("Closed Connection: go back to top of loop");
                	socket.close();
                	continue;
                } catch (HaltException e) {
                    HttpIoHandler.sendException(socket, request, e);
                    socket.close();
                    continue;
                }

                // Update the task this worker is working on
                task(request.url()); 
                logger.info("Serving " + this.task);
                
                // Send 100 Continue response if appearing in the request header
                String protocol = request.protocol();
                String expect = request.headers("expect");
                if (protocol.equals("HTTP/1.1") && expect != null && expect.toLowerCase().equals("100-continue")) {
                    Response response = new HttpResponse();
                    response.status(100);
                    HttpIoHandler.sendResponse(socket, request, response);
                }

                String method = request.requestMethod();
                String pathInfo = request.pathInfo();
                
                // Only supports GET & HEAD for the moment: delete this in MILESTONE 2
                if (!method.equals("GET") && !method.equals("HEAD")) {
                    throw new HaltException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not Implemented");
                }
                
                // Handle the shut down request
                if (method.equals("GET") && pathInfo.equals("/shutdown")) {
                    logger.info("Shutting down server...");
                    webService.stop();
                    logger.info("Server shut down gracefully");
                    socket.close();
                    return; 
                }

                RequestHandler handler;
                if (pathInfo.equals("/control")) {
                    handler = new ControlRequestHandler(webService);
                } else { 
                    handler = new StaticFileRequestHandler(webService);
                }
                try {
                    Response response = handler.handle(request);
                    HttpIoHandler.sendResponse(socket, request, response);
                } catch (HaltException e) {
                    HttpIoHandler.sendException(socket, request, e);
                }
                logger.debug("===================== Task finished!! ========================");
                socket.close();
            } catch (InterruptedException e) {
                logger.info("InterruptedException thrown while dequeing a task - Normal behavior if we are shutting down");
            } catch (IOException ioe) {
                logger.warn("Fail to close the socket");
            } finally {
                task(null); // Reset task to null
            }
        }
    }
}
