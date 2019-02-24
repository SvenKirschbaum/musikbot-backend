<% 
	if(request.getAttribute("worked") == null) {
		response.sendRedirect("/");
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.TimeZone"%>
<%@ page import="de.elite12.musikbot.server.core.*"%>
<%@ page import="de.elite12.musikbot.server.util.*"%>
<%@ page import="de.elite12.musikbot.server.model.*"%>
<%@ page import="de.elite12.musikbot.server.controller.*"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.LinkedList"%>
<%@ page import="java.util.UUID"%>
<%@ page import="org.springframework.web.util.HtmlUtils"%>
<%@ page import="java.net.URL"%>
<%@ page import="java.net.URLEncoder"%>
<%
	Cookie[] co = request.getCookies();
	String style = "radio";
	if(co != null) {
		for(Cookie c:co) {
			if(c.getName().equals("style")) {
				style = c.getValue();
			}
		}
	}
	URL u = Controller.class.getClassLoader().getResource("de/elite12/musikbot/server/resources/styles/"+style+".css");
	if(u == null) {
		style = "radio";
	}
	
	
	User user = SessionHelper.getUserFromSession(session);
%>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8">
<meta name="ROBOTS" content="INDEX, FOLLOW">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="Website des Elite12-Musikbots">
<link rel="stylesheet" type="text/css" href="/res/styles/<%= style %>.css" />
<link rel="shortcut icon" href="/res/favicon.png">
<script defer type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.css">
<script defer src="//ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script>
<% if(user != null) { %>
<script type="text/javascript">
	var authtoken = "<%= (String) session.getAttribute("token") %>";
</script>
<% } %>
<script defer type="text/javascript" src="/res/main.js"></script>
<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  <% if(user != null) { %>
  ga('create', 'UA-60228333-1', { 'userId': '<%= user.hashCode() %>'});
  <% } else { %>
  ga('create', 'UA-60228333-1', 'auto');
  <% } %>
  ga('set', 'anonymizeIp', true);
  ga('send', 'pageview');
</script>
<link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/cookieconsent2/3.0.3/cookieconsent.min.css" />
<script src="//cdnjs.cloudflare.com/ajax/libs/cookieconsent2/3.0.3/cookieconsent.min.js"></script>
<link rel="alternate" type="application/rss+xml" title="RSS" href="https://musikbot.elite12.de/feed/" />