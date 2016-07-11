<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<% 
	if(request.getAttribute("worked") == null) {
		response.sendRedirect("/");
		return;
	}
%>
<%@ page import="de.elite12.musikbot.server.*"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.LinkedList"%>
<div id="messages">
	<% 
		@SuppressWarnings("unchecked")
		List<UserMessage> msglist = (List<UserMessage>)request.getSession().getAttribute("msg");
		if(msglist == null) {
			msglist = new LinkedList<UserMessage>();
		}
		int i = 0;
		for(UserMessage msg : msglist) {
			%>
	<div id="msg_<%= i %>" class="msg_<%=msg.gettype()%>"><%= msg.getMsg() %><div
			class="close_button" onclick="closemsg(<%= i %>)"></div>
	</div>
	<%
			i++;
		}
		msglist.clear();
		request.getSession().setAttribute("msg", msglist);
	%>
</div>