package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.m1.handling.DateUtils;
import edu.upenn.cis.cis455.m1.interfaces.Response;


public class HttpResponse extends Response {

    public HttpResponse() {
        super();
    }

    @Override
    public String getHeaders() {
        StringBuilder sb = new StringBuilder("");
        sb.append("Date: " + DateUtils.getCurrentTime() + "\n");
        sb.append("Server: " + server() + "\n");
        sb.append("Last-Modified: " + DateUtils.unixToDate(lastModified) + "\n");
        sb.append("Content-Type: " + type() + "\n");
        sb.append("Content-Length: " + length() + "\n");
        return sb.toString();
    }
}
