<?xml version="1.0" encoding="UTF-8"?>
<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/xml;charset=UTF-8" language="java"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ page import="java.util.Date"%>
<%@ page import="org.springframework.web.util.HtmlUtils"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils"%>

<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
	<channel>
		<title>Elite12 - Radio</title>
		<description>Playlist des Elite12 Musikbots</description>
		<language>de-de</language>
		<link>https://musikbot.elite12.de/feed/</link>
		<lastBuildDate><%=new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z",Locale.US).format(new Date())%></lastBuildDate>
		<generator>Elite12 - Musikbot</generator>
		<managingEditor>admin@elite12.de (Sven Kirschbaum)</managingEditor>
		<webMaster>admin@elite12.de (Sven Kirschbaum)</webMaster>
		<atom:link
			href="https://musikbot.elite12.de/feed/" rel="self"
			type="application/rss+xml" /> 
		<%
 			ResultSet rs = (ResultSet) request.getAttribute("result");
 			while (rs.next()) {
 		%> 
 			<item> 
 				<title><%=StringEscapeUtils.escapeXml(rs.getString("SONG_NAME"))%></title>
				<description><%=StringEscapeUtils.escapeXml(rs.getString("SONG_NAME"))%></description>
				<link><%=StringEscapeUtils.escapeXml(rs.getString("SONG_LINK"))%></link>
				<pubDate><%=new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z",Locale.US).format(rs.getTimestamp("SONG_INSERT_AT"))%></pubDate>
				<guid isPermaLink="false">e12musikbotentry<%=rs.getInt("SONG_ID")%></guid>
			</item> 
		<%
 			}
 		%>
	</channel>
</rss>