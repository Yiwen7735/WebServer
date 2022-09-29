package edu.upenn.cis.cis455.m1.server;

import java.util.Map;

import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

public class ControlRequestHandler implements RequestHandler {

    private static final String htmlTemplate =
        "<!DOCTYPE html>" + 
        "<html>" + 
            "<head>" + 
                "<title>Control Panel</title>" +
            "</head>" + 
            "<body>" +
                "<h1>Control Panel</h1>" + 
                "<table style=\"width: 600px;\">" +
                "<tr>" + 
                    "<th style=\"width: 150px;\">Thread ID</th>" + 
                    "<th style=\"width: 150px;\">Thread Name</th>" + 
                    "<th style=\"width: 300px;\">Thread Status</th>" + 
                "</tr>" + 
                "$list" + 
                "</table>" + 
                "<br/>" + 
                "<a href=\"/shutdown\">Shut down</a>" +
            "</body>" +
        "</html>";

    protected WebService webService;

    public ControlRequestHandler(WebService webService) {
        this.webService = webService;
    }

    @Override
    public Response handle(Request request) {
        StringBuilder threadInfo = new StringBuilder("");
        Map<String, String> panel = webService.workerPool.panel();
        for (String key: panel.keySet()) {
            String[] keys = key.split("\\s+");
            threadInfo.append("<tr>");
            threadInfo.append( 
                "<td style=\"width: 150px; text-align: center;\">" + keys[0] + "</td>" + 
                "<td style=\"width: 150px; text-align: center;\">" + keys[1] + "</td>" + 
                "<td style=\"width: 300px; text-align: center;\">" + panel.get(key) + "</td>"
            );
            threadInfo.append("</tr>");
        }
        String htmlStr = htmlTemplate.replace("$list", threadInfo.toString());
        Response response = new HttpResponse();
        response.body(htmlStr);
        response.type("text/html");
        response.lastModified(System.currentTimeMillis());
        return response;
    }
}
