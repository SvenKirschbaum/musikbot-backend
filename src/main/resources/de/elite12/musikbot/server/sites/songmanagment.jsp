<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.jsp"%>
<title>Elite12 - Musikbot</title>
</head>
<body>
	<div id="topic"></div>
	<div id="content">

		<div id="content2">

			<%@ include file="messages.jsp"%>
			<form action="/songs/" method="post">
				<input type="hidden" name="action" value="delete" />
				<table class="bordered banned" id="playlist">
					<thead>
						<tr>
							<th>ID</th>
							<th>Name</th>
							<th>VID</th>
							<th>L&ouml;schen?</th>
						</tr>
					</thead>
					<tbody>
						<%
							ResultSet rs = (ResultSet) request.getAttribute("result");
							while (rs.next()) {
						%>
						<tr>
							<td><%=rs.getInt("id")%></td>
							<td><%=rs.getString("SONG_NAME")%></td>
							<td><a
								href="http://www.youtube.com/watch?v=<%=rs.getString("YTID")%>"><%=rs.getString("YTID")%></a></td>
							<td><input type="checkbox" name="song"
								value="<%=rs.getInt("id")%>" /></td>
						</tr>
						<%
							}
						%>
					</tbody>
				</table>
				<input id="deletebutton" type="submit" value="L&ouml;schen">
			</form>
			<form id="bansong" class="bordered" method="post" action="/songs/">
				<input type="hidden" name="action" value="add" /> Song: <input
					type="text" name="song" /> <input id="" type="submit" name="sub"
					value="Sperren" />
			</form>
		</div>
	</div>
	<%@ include file="footer.jsp"%>