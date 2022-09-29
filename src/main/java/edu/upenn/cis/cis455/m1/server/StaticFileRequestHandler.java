package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.m1.handling.DateUtils;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StaticFileRequestHandler implements RequestHandler {

	static final Logger logger = LogManager.getLogger(StaticFileRequestHandler.class);

    private WebService webService;

	public StaticFileRequestHandler(WebService webService) {
        this.webService = webService;
	} 

	private static String contentType(Path filePath) {
		String path = filePath.toString();
		String extension = path.substring(path.lastIndexOf('.') + 1);
		switch (extension) {
			case "jpg":
			case "jpeg":
			case "jfjf": 
			case "pjpeg":
			case "pjp":
				return "image/jpeg";
			case "png":
				return "image/png";
			case "gif":
				return "image/gif";
			case "txt":
				return "text/plain";
			case "css":
				return "text/css";
			case "html":
				return "text/html";
			case "js":
				return "text/javascript";
			default:
				return "text/plain";
		}
	}

	@Override
	public Response handle(Request request) {
        String root = webService.staticFileLocation();
		String path = request.pathInfo();
		Path filePath;

		// Throw a bad request if no valid path can be formed
		try {
			filePath = Paths.get(root, path).normalize();
		} catch (InvalidPathException e) {
            webService.halt(HttpServletResponse.SC_BAD_REQUEST);
			return null;
        }
		
		logger.debug("root, filePath: " + root + ", " + filePath.toString());

		// Throw a forbidden request if the resulting file path is above the root
		if (!filePath.toString().startsWith(root)) {
			webService.halt(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}

		// If a directory is requested, look for the index.html file
		if (Files.isDirectory(filePath)) {
			Path indexFilePath  = Paths.get(filePath.toString(), "index.html");
			if (Files.exists(indexFilePath)) {
				filePath = indexFilePath;
				logger.debug("filePath: " + filePath);
			}
		}
		// If the file/dir path does not exist, throw a Not Found error
		if (!Files.exists(filePath)) {
			webService.halt(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		// Check If-Modified-Since/If-Unmodified-Since header
		long lastModified = filePath.toFile().lastModified();
		String date = request.headers("if-modified-since");

		if (date != null && lastModified < DateUtils.dateToUnix(date)) {
			webService.halt(HttpServletResponse.SC_NOT_MODIFIED, "Not Modified");
			return null;
		}

		date = request.headers("if-unmodified-since");
		if (date != null && lastModified > DateUtils.dateToUnix(date)) {
			webService.halt(HttpServletResponse.SC_PRECONDITION_FAILED, "Precondition Failed");
			return null;
		} 

		Response response = null;

		if (Files.isDirectory(filePath)) {
			return handleDirectory(filePath, Paths.get(path).normalize(), lastModified);
		}

		// Something wrong while reading in the data, throw a server error
		try {
			byte[] content = Files.readAllBytes(filePath);
			response = new HttpResponse();
			response.bodyRaw(content);
			response.type(contentType(filePath));
			response.lastModified(lastModified);
		} catch (IOException e) {
			webService.halt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private Response handleDirectory(Path filePath, Path relPath, long lastModified) {

		final String htmlTemplate =
        "<!DOCTYPE html>" + 
        "<html>" + 
            "<head>" + 
                "<title>Directory</title>" +
            "</head>" + 
            "<body>" +
                "<h1>Directory</h1>" + 
                "<ul>" + 
                    "$list" +  
                "</ul>" + 
            "</body>" +
        "</html>";

		String[] files = filePath.toFile().list();
		StringBuilder sb = new StringBuilder("");
		for (String fn: files) {
			sb.append("<li><a href=\"" + relPath + "/" + fn + "\">" + fn + "</a></li>");
		}

		Response response = new HttpResponse();
		response.type("text/html");
		response.body(htmlTemplate.replace("$list", sb.toString()));
		response.lastModified(lastModified);
		return response;
	}

}
