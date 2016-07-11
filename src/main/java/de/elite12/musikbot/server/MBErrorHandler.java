package de.elite12.musikbot.server;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;

public class MBErrorHandler extends
		org.eclipse.jetty.server.handler.ErrorHandler {

	@SuppressWarnings("unused")
	private Controller ctr;

	public MBErrorHandler(Controller con) {
		this.ctr = con;
		this.setShowStacks(false);
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		super.handle(target, baseRequest, request, response);
		Logger.getLogger(MBErrorHandler.class).warn(
				"Handling Error for User: "
						+ request.getSession().getAttribute("user")
						+ " Error Code: " + response.getStatus() + " Message: "
						+ HttpStatus.getMessage(response.getStatus())+ " Path: "
						+ request.getRequestURI()+(request.getQueryString()==null?"":"?"+request.getQueryString()));
	}

	@Override
	protected void writeErrorPage(HttpServletRequest request, Writer writer,
			int code, String message, boolean showStacks) throws IOException {
		if (message == null) {
			message = HttpStatus.getMessage(code);
		}
		writer.write("<html>\n<head>\n");
		writeErrorPageHead(request, writer, code, message);
		writer.write("</head>\n<body class=\"error\" title=\"Zur&uuml;ck zur Startseite\"><a href=\"/\">");
		writeErrorPageBody(request, writer, code, message, showStacks);
		writer.write("\n</a></body>\n</html>\n");
	}

	@Override
	protected void writeErrorPageHead(HttpServletRequest request,
			Writer writer, int code, String message) throws IOException {
		super.writeErrorPageHead(request, writer, code, message);
		writer.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/res/styles/radio.css\" />");
	}
}
