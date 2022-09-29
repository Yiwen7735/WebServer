package edu.upenn.cis.cis455.m1.server;

import java.util.Map;
import java.util.Set;

import edu.upenn.cis.cis455.m1.interfaces.Request;

public class HttpRequest extends Request {
    
    private final String requestMethod, host, url, uri, pathInfo, protocol, ip, body;
    private final int port;
    private final Map<String, String> headers;
    
    public HttpRequest(
        String requestMethod,
        String host,
        String url,
        String uri,
        String pathInfo, 
        String protocol,
        String ip,
        String body,
        int port,
        Map<String, String> headers
    ) {
        this.requestMethod = requestMethod;
        this.host = host;
        this.url = url;
        this.uri = uri;
        this.pathInfo = pathInfo;
        this.protocol = protocol;
        this.ip = ip;
        this.body = body;
        this.port = port;
        this.headers = headers;
    }

    public String requestMethod() {
        return requestMethod;
    }

    public String host() {
        return host;
    }

    public String userAgent() {
        return headers.get("user-agent");
    }

    public int port() {
        return port;
    }

    public String pathInfo() {
        return pathInfo;
    }

    public String url() {
        return url;
    }

    public String uri() {
        return uri;
    }

    public String protocol() {
        return protocol;
    }

    public String contentType() {
        return headers.get("content-type");
    }

    public String ip() {
        return ip;
    }

    public String body() {
        return body;
    }

    public int contentLength() {
        String len = headers.get("content-length");
        return len == null ? 0 : Integer.parseInt(len);
    }

    public String headers(String name) {
        return headers.get(name.toLowerCase());
    }

    public Set<String> headers() {
        return headers.keySet();
    }
}
