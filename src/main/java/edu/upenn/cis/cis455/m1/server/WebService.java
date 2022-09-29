/**
 * CIS 455/555 route-based HTTP framework
 * 
 * V. Liu, Z. Ives
 * 
 * Portions excerpted from or inspired by Spark Framework, 
 * 
 *                 http://sparkjava.com,
 * 
 * with license notice included below.
 */

/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.upenn.cis.cis455.m1.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.exceptions.HaltException;

public class WebService {
    final static Logger logger = LogManager.getLogger(WebService.class);

    protected int port = 45555;
    protected String root = "./www"; // Root directory of the files
    protected String ipAddress = "0.0.0.0";

    protected HttpListener listener; // Producer of task
    protected Thread listenerThread;

    protected int threads;
    protected HttpWorkerPool workerPool;  // Consumers of task
    protected final HttpTaskQueue taskQueue = new HttpTaskQueue();

    /**
     * Launches the Web server thread pool and the listener
     */
    public void start() {
        
        try {
            // Set up a listener first with a port number and ipAddress
            listener = new HttpListener(port, ipAddress, taskQueue);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Starting listener...");
        listenerThread = new Thread(listener);
        listenerThread.start(); 
        logger.info("Listener started");

        logger.info("Starting worker pool...");
        workerPool = new HttpWorkerPool(threads, taskQueue, this);
        logger.info("Worker pool started");
    }
 
    /**
     * Gracefully shut down the server
     */
    public void stop() {
    	
    	logger.info("Shutting down listener...");
        listener.stop(); // Set the stop flag to true
        try {
            listenerThread.join();
        } catch (InterruptedException e) {
            halt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        logger.info("Listener shut down gracefully");
        
    	
    	logger.info("Shutting down worker pool...");
        while (taskQueue.taskQueue.size() != 0) {
            // wait for all the remaining tasks to be processed
        }
        workerPool.stop();
        logger.info("Worker pool shut down gracefully");
        
        
        // At the end, close the ServerSocket connection only
        // after all workers have terminated gracefully, 
        // ensuring no loss of tasks.
        logger.info("Closing connection with client...");
        try {
			listener.close();
		} catch (IOException e) {
			logger.warn("Failed to close Server Socket");
		}
        logger.info("Connection with client closed gracefully");
    }

    /**
     * Hold until the server is fully initialized.
     * Should be called after everything else.
     */
    public void awaitInitialization() {
        logger.info("Initializing server");
        start();
    }

    /**
     * Triggers a HaltException that terminates the request
     */
    public HaltException halt() {
        throw new HaltException();
    }

    /**
     * Triggers a HaltException that terminates the request
     */
    public HaltException halt(int statusCode) {
        throw new HaltException(statusCode);
    }

    /**
     * Triggers a HaltException that terminates the request
     */
    public HaltException halt(String body) {
        throw new HaltException(body);
    }

    /**
     * Triggers a HaltException that terminates the request
     */
    public HaltException halt(int statusCode, String body) {
        throw new HaltException(statusCode, body);
    }

    ////////////////////////////////////////////
    // Server configuration
    ////////////////////////////////////////////

    /**
     * Set the root directory of the "static web" files
     */
    public void staticFileLocation(String directory) {
        // Convert to an absolute root path, e.g., "/var/www"
        File file = new File(directory); 
        root = Paths.get(file.getAbsolutePath()).normalize().toString();
    }

    public String staticFileLocation() {
        return root;
    }

    /**
     * Set the IP address to listen on (default 0.0.0.0)
     */
    public void ipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Set the TCP port to listen on (default 45555)
     */
    public void port(int port) {
        this.port = port;
    }

    /**
     * Set the size of the thread pool
     */
    public void threadPool(int threads) {
        this.threads = threads;
    }

}
