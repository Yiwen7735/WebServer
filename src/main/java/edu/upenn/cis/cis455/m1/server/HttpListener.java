package edu.upenn.cis.cis455.m1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stub for your HTTP server, which listens on a ServerSocket and handles
 * requests
 */
public class HttpListener implements Runnable {

    static final Logger logger = LogManager.getLogger(HttpListener.class);

    volatile protected boolean stop = false;

    protected ServerSocket socket;
    protected String ipAddress;
    protected HttpTaskQueue taskQueue;

    public HttpListener(int port, String ipAddress, HttpTaskQueue queue) throws IOException {
        socket = new ServerSocket(port);
        socket.setSoTimeout(1000);
        this.ipAddress = ipAddress;
        taskQueue = queue;
    } 

    /**
     * Gracefully terminate listener's work
     */
    public void stop() {
        stop = true;
    }
    
    /**
     * Close the server socket connection
     * @throws IOException
     */
    public void close() throws IOException {
    	socket.close();
    }
    
    @Override
    public void run() {
        while (!stop) {
            try {
                Socket request = socket.accept();
                HttpTask task = new HttpTask(request);
                taskQueue.enqueue(task);
            } catch (SocketTimeoutException e) {
                // Don't really need to handle this
            } catch (IOException e) {
                logger.warn("IOException thrown while connecting to client");
            } catch (InterruptedException e) {
                logger.info("InterruptedException thrown while enqueueing a tas");
            }
        }
    }
}
