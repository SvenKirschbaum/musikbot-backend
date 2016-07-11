<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.jsp"%>
<%
		ResultSet rs = (ResultSet) request.getAttribute("result");
		SimpleDateFormat timeformat = new SimpleDateFormat(
				"dd.MM.yyyy - HH:mm:ss");
		timeformat.setTimeZone(TimeZone.getDefault());
%>
<%!
	private String getPager(int n, HttpServletRequest request) {
		if(n > 0 && n <= (int)request.getAttribute("seiten")) {
			return "<a href=\"/archiv/?p="+ n +"\">"+n+"</a>";
		}
		return "";
	}
%>
<title>Elite12 - Musikbot - Archiv</title>
</head>
<body>
	<div id="topic"></div>
	<div id="content">

		<div id="content2">

			<%@ include file="messages.jsp"%>

			<table class="bordered paginated" id="playlist">
				<thead>
					<tr>
						<th>Song ID</th>
						<th>Gespielt um</th>
						<th>Eingefügt von</th>
						<th>Titel</th>
						<th>Link</th>
					</tr>
				</thead>
				<tbody>
					<%
						while (rs.next()) {
					%>
					<tr
						class='songentry <%if (rs.getBoolean("SONG_SKIPPED") == true) {%>skipped<%}%>'
						id='song_<%=rs.getInt("SONG_ID")%>'
						<%if (rs.getBoolean("SONG_SKIPPED") == true) {%>
						title="Dieses Lied wurde nicht bis zum Ende gespielt!" <%}%>>
						<td style="text-align: center"><%=rs.getInt("SONG_ID")%></td>
						<td><%=timeformat.format(rs.getObject("SONG_PLAYED_AT"))%></td>
						<td>
							<%
								try {
											UUID id = UUID.fromString(rs.getString("AUTOR"));
							%>Gast<%
								} catch (IllegalArgumentException e) {
							%> <%=rs.getString("AUTOR")%> <%
								}
							%>
						</td>
						<td title="<%=HtmlUtils.htmlEscape(rs.getString("SONG_NAME"))%>"><%=rs.getString("SONG_NAME").length() > 60 ? HtmlUtils.htmlEscape(rs
							.getString("SONG_NAME").substring(0, 60)) + "..."
							: HtmlUtils.htmlEscape(rs.getString("SONG_NAME"))%></td>
						<td><a href='<%=rs.getString("SONG_LINK")%>' target="_blank"><%=rs.getString("SONG_LINK")%></a></td>
					</tr>
					<%
						}
					%>

				</tbody>
			</table>
			<div id="archivlink" class="link">
				<a href="/">Zur Playlist</a>
			</div>
			<div class="pager">
				<a href="/archiv/">First</a>
				<%= this.getPager((int)request.getAttribute("page")-3, request) %>
				<%= this.getPager((int)request.getAttribute("page")-2, request) %>
				<%= this.getPager((int)request.getAttribute("page")-1, request) %>
				<a class="currentpage"
					href="/archiv/?p=<%= request.getAttribute("page") %>"><%= request.getAttribute("page") %></a>
				<%= this.getPager((int)request.getAttribute("page")+1, request) %>
				<%= this.getPager((int)request.getAttribute("page")+2, request) %>
				<%= this.getPager((int)request.getAttribute("page")+3, request) %>
				<a href="/archiv/?p=<%= request.getAttribute("seiten") %>">Last</a>
			</div>
		</div>
	</div>
	<%@ include file="footer.jsp"%>