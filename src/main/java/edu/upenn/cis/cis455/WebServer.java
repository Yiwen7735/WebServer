package edu.upenn.cis.cis455;

import static edu.upenn.cis.cis455.SparkController.*;

import org.apache.logging.log4j.Level;

/**
 * Initialization / skeleton class.
 * Note that this should set up a basic web server for Milestone 1.
 * For Milestone 2 you can use this to set up a basic server.
 * 
 * CAUTION - ASSUME WE WILL REPLACE THIS WHEN WE TEST MILESTONE 2,
 * SO ALL OF YOUR METHODS SHOULD USE THE STANDARD INTERFACES.
 * 
 * @author zives
 *
 */
public class WebServer {
    public static void main(String[] args) {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);

        // TODO: make sure you parse *BOTH* command line arguments properly
        int port = Integer.parseInt(args[0]);
        String root = args[1];
        
        // Configure in SparkController
        port(port);
        staticFileLocation(root);
        threadPool(13);
        ipAddress("0.0.0.0");

        // All user routes should go below here...

        // ... and above here. Leave this comment for the Spark comparator tool

        System.out.println("Waiting to handle requests!");
        awaitInitialization();

    }
}
