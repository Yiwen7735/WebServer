package edu.upenn.cis.cis455.m1.server;

import java.lang.InterruptedException;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HttpWorkerPool {

    static final Logger logger = LogManager.getLogger(HttpWorker.class);

    protected HttpWorker[] workers;
    protected Thread[] workerThreads;

    /**
     * Initialize and start all worker threads with a given pool size,
     * a task queue and the web service on which the workers are operating
     */
    public HttpWorkerPool(int size, HttpTaskQueue queue, WebService webService) {
        workers = new HttpWorker[size];
        workerThreads = new Thread[workers.length];
        for (int i = 0; i < size; i++) {
            workers[i] = new HttpWorker(queue, webService);
            workerThreads[i] = new Thread(workers[i]);
            logger.info("Starting worker " + workerThreads[i].getName());
            workerThreads[i].start();
        }
    }

    /**
     * Gracefully terminate each worker's work
     */
    public void stop() {
        // Get the id of the currently running thread (i.e., doing shutdown work)
        long shutdownId = Thread.currentThread().getId();
        for (int i = 0; i < workers.length; i++) {
            // DO NOT stop the thread responsible for shutting down!!
            if (workerThreads[i].getId() == shutdownId) {
                continue;
            }
            logger.info("Terminating worker " + workerThreads[i].getName());
            
            // Set the stop flag to true
            workers[i].stop();
            
            // Call interrupt() to wake up threads in case they are blocked or
            // waiting, an InterruptedException will be caught in worker.run()
            // and the thread will exit run() since the stop flag was set to true
            // Note that if the thread is running, no InterruptedException is thrown
            // and the thread will run to completion (so no tasks interrupted!)
            workerThreads[i].interrupt(); 
            
            // The thread responsible for shutting down waits on thread # i
            try {
                workerThreads[i].join();  
            } catch (InterruptedException e) {
                logger.warn("The thread responsible for shutting down was interrupted");
            }
            logger.info("Worker " + workerThreads[i].getName() + " terminated gracefully");
        }
    }

    /**
     * @return the thread pool of all workers
     */
    public Thread[] pool() {
        return workerThreads;
    }

    /**
     * @return a map <id+name, status> of information for all threads
     */
    public Map<String, String> panel() {
        Map<String, String> panel = new HashMap<>();
        for (int i = 0; i < workerThreads.length; i++) {
            String key = workerThreads[i].getId() + " " + workerThreads[i].getName();
            String task = workers[i].task;
            panel.put(key, task == null ? "waiting" : task);
        }
        return panel;
    }
}
