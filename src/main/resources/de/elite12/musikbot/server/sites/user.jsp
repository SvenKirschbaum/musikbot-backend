
<%
	if (request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.jsp"%>
<%@ page import="de.elite12.musikbot.server.controller.Weblet.TopEntry"%>
<%@ page import="de.elite12.musikbot.server.controller.UserServlet.DataEntry"%>
<title><%= ((User) request.getAttribute("viewuser")).getName() %> - Elite12 - Musikbot</title>
</head>
<body>
	<div id="backlink" class="link">
		<a href="/">Zurück</a>
	</div>
	<div id="changedialog" title="Dialog Form">
		<form action="" method="post">
			<label>Wert:</label>
			<input class="value" id="value" name="value" type="text">
			<input class="value" id="pvalue" name="pvalue" type="password">
			<input id="submit" type="submit" value="Ändern">
		</form>
	</div>
	<div id="topic"></div>
	<div id="usercontainer">
		<div id="pbild" class="bordered">
			<img alt="profilbild"
				src="https://www.gravatar.com/avatar/<%=Util.md5Hex(((User) request.getAttribute("viewuser")).getEmail().toLowerCase(Locale.GERMAN))%>?s=350&d=<%=URLEncoder.encode("https://musikbot.elite12.de/res/favicon.png","UTF-8") %>" />
		</div>
		<div class="listwrapper">
		<div id="userstats" class="bordered">
		<div class="statsheadline">Allgemein:</div>
			<table>
				<tbody>
					<%
					for(DataEntry e:((DataEntry[])request.getAttribute("userinfo"))) {
				%>
					<tr id="<%=e.urlname%>">
						<td><%= e.name %></td>
						<td><span><%= e.value %></span><% if(e.changeable){ %>    <a href="#" onclick='showform("<%=e.urlname%>")'>(Ändern)</a><% } %></td>
					</tr>
					<%
					}
				%>
				</tbody>
			</table>
		</div>
		<div id="recent" class="bordered">
			<div class="statsheadline">Most recent:</div>
			<table>
				<thead>
					<tr>
						<th>Nr.</th>
						<th>Titel</th>
					</tr>
				</thead>
				<tbody>
					<% 
					for(TopEntry e:((List<TopEntry>)request.getAttribute("recent"))) {
				%>
					<tr>
						<td><%= e.getCount() %></td>
						<td class="link" title="<%= HtmlUtils.htmlEscape(e.getName()) %>"><a
							href="<%= e.getLink() %>"><%= e.getName().length() > 60 ? HtmlUtils.htmlEscape(e.getName().substring(0, 60)) + "..." : HtmlUtils.htmlEscape(e.getName()) %></a></td>
					</tr>
					<%
					}
				%>
				</tbody>
			</table>
		</div>
		</div>
		<div class="listwrapper"><div id="topsongs" class="bordered">
			<div class="statsheadline">Am meisten gewünscht:</div>
			<table>
				<thead>
					<tr>
						<th>Nr.</th>
						<th>Titel</th>
						<th>Anzahl</th>
					</tr>
				</thead>
				<tbody>
					<% 
					int i = 1;
					for(TopEntry e:((List<TopEntry>)request.getAttribute("toplist"))) {
				%>
					<tr>
						<td><%= i %>.</td>
						<td class="link" title="<%= HtmlUtils.htmlEscape(e.getName()) %>"><a
							href="<%= e.getLink() %>"><%= e.getName().length() > 60 ? HtmlUtils.htmlEscape(e.getName().substring(0, 60)) + "..." : HtmlUtils.htmlEscape(e.getName()) %></a></td>
						<td><%= e.getCount() %></td>
					</tr>
					<%
						i++;
					}
				%>
				</tbody>
			</table>
		</div>
		<div id="topskipped" class="bordered">
			<div class="statsheadline">Am meisten geskippt:</div>
			<table>
				<thead>
					<tr>
						<th>Nr.</th>
						<th>Titel</th>
						<th>Anzahl</th>
					</tr>
				</thead>
				<tbody>
					<% 
					i = 1;
					for(TopEntry e:((List<TopEntry>)request.getAttribute("topskipped"))) {
				%>
					<tr>
						<td><%= i %>.</td>
						<td class="link" title="<%= HtmlUtils.htmlEscape(e.getName()) %>"><a
							href="<%= e.getLink() %>"><%= e.getName().length() > 60 ? HtmlUtils.htmlEscape(e.getName().substring(0, 60)) + "..." : HtmlUtils.htmlEscape(e.getName()) %></a></td>
						<td><%= e.getCount() %></td>
					</tr>
					<%
						i++;
					}
				%>
				</tbody>
			</table>
		</div>
	</div></div>
	<%@ include file="footer.jsp"%>