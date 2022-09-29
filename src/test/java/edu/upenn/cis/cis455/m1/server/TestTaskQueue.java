package edu.upenn.cis.cis455.m1.server;

import java.net.Socket;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;


public class TestTaskQueue {
    
    HttpTaskQueue taskQueueObj;
    Socket socket = mock(Socket.class);;
    
    @Before
    public void setUp() {
        taskQueueObj = new HttpTaskQueue();
    }
    
    @Test
    public void testEnqueue() throws InterruptedException {
        
        HttpTask task = new HttpTask(socket);
        taskQueueObj.enqueue(task);
        assertEquals(taskQueueObj.taskQueue.size(), 1);
        assertEquals(taskQueueObj.taskQueue.firstElement(), task);
    }
    
    @Test
    public void testDeque() throws InterruptedException {
        HttpTask task1 = new HttpTask(socket);
        HttpTask task2 = new HttpTask(socket);
        taskQueueObj.enqueue(task1);
        taskQueueObj.enqueue(task2);
        
        assertEquals(taskQueueObj.dequeue(), task1);
        assertEquals(taskQueueObj.dequeue(), task2);
        assertEquals(taskQueueObj.taskQueue.size(), 0);
    }
}
