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
package edu.upenn.cis.cis455.m1.interfaces;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

public abstract class Response {
 
    public static final String PROTOCOL = "HTTP/1.1";

    public static final Map<Integer, String> STATUS_MSG = new HashMap<>();
    static {
        STATUS_MSG.put(HttpServletResponse.SC_CONTINUE, "Continue");
        STATUS_MSG.put(HttpServletResponse.SC_OK, "OK");
        STATUS_MSG.put(HttpServletResponse.SC_MOVED_PERMANENTLY, "Moved Permanently");
        STATUS_MSG.put(HttpServletResponse.SC_MOVED_TEMPORARILY, "Moved Temporarily");
        STATUS_MSG.put(HttpServletResponse.SC_SEE_OTHER, "See Other");
        STATUS_MSG.put(HttpServletResponse.SC_NOT_MODIFIED, "Not Modified");
        STATUS_MSG.put(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
        STATUS_MSG.put(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        STATUS_MSG.put(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        STATUS_MSG.put(HttpServletResponse.SC_NOT_FOUND, "Not Found");
        STATUS_MSG.put(HttpServletResponse.SC_PRECONDITION_FAILED, "Precondition Failed");
        STATUS_MSG.put(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
        STATUS_MSG.put(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not Implemented");
    }

    protected int statusCode = 200;
    protected byte[] body;
    protected String contentType = null; // e.g., "text/plain";

    protected String server = null;
    protected long lastModified; // unix epoch time in ms

    public int status() {
        return statusCode;
    }

    public void status(int statusCode) {
        this.statusCode = statusCode;
    }

    public String body() {
        try {
            return body == null ? "" : new String(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public byte[] bodyRaw() {
        return body;
    }

    public void bodyRaw(byte[] b) {
        body = b;
    }

    /**
     * Body passed as string data, which will be 
     * converted to byte array
     * @param body
     */
    public void body(String body) {
        this.body = body == null ? null : body.getBytes();
    }

    public String type() {
        return contentType;
    }

    public void type(String contentType) {
        this.contentType = contentType;
    }

    public int length() {
        return this.body == null ? 0 : body.length;
    }
    
    public void server(String server) {
        this.server = server;
    }
    
    public String server() {
        return server;
    }

    public void lastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long lastModified() {
        return lastModified;
    }

    public String responseLine() {
        return PROTOCOL + " " + statusCode + " " + STATUS_MSG.get(statusCode) + "\n";
    }

    public abstract String getHeaders();
}
