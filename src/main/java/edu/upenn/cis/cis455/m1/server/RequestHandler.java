package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

public interface RequestHandler {
    public Response handle(Request request);
}
