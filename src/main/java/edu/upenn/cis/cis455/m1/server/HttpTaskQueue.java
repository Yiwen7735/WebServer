package edu.upenn.cis.cis455.m1.server;

import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stub class for implementing the queue of HttpTasks
 */
public class HttpTaskQueue {
	static final Logger logger = LogManager.getLogger(HttpTaskQueue.class);

	protected static final int QUEUE_SIZE = 1000;
	
	protected final Vector<HttpTask> taskQueue = new Vector<>();
	
	public void enqueue(HttpTask task) throws InterruptedException {
		logger.info("Adding a new task to the queue");
		while (true) {
			synchronized (taskQueue) {
				if (taskQueue.size() == QUEUE_SIZE) {
					logger.debug("Queue is currently full, back to waiting");
					taskQueue.wait();
				} else {
					taskQueue.add(task);
					logger.debug("Task added: " + task);
					taskQueue.notifyAll();
					break;
				}
			}
		}
	} 

	public HttpTask dequeue() throws InterruptedException {
		logger.info("Looking for a task in the queue");
		while (true) {
			synchronized (taskQueue) {
				if (taskQueue.isEmpty()) {
					logger.debug("Queue is currently empty, back to waiting");
					taskQueue.wait();
				} else {
					HttpTask task = taskQueue.remove(0);
					logger.debug("Task removed: " + task);
					taskQueue.notifyAll();
					return task;
				}
			}
		}
	}
}
