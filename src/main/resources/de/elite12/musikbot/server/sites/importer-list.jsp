<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="de.elite12.musikbot.server.PlaylistImporter.Playlist"%>
<%@page import="de.elite12.musikbot.server.PlaylistImporter.Playlist.Entry"%>
<%@ include file="header.jsp"%>
<title>Elite12 - Musikbot</title>
</head>
<body class="bimport">
	<div id="topic"></div>
	<div id="content">

		<div id="content2">

			<%@ include file="messages.jsp"%>
			<form method="post" action="/import/">
				<div id="import_list" class="bordered">
					<div id="headline">
						Import der Playlist: <a
							href="<%=((Playlist) request.getAttribute("playlist")).link %>"><%=((Playlist) request.getAttribute("playlist")).name %></a>
					</div>

					<input type="hidden" name="import" value="finish" /> <input
						type="hidden" name="playlist"
						value="<%=((Playlist) request.getAttribute("playlist")).link%>" />
					<table id="import_table">
						<thead>
							<tr>
								<th>Importieren?<input type="checkbox"
									onclick="var selected = this.checked;$(':checkbox').each(function () {    this.checked = selected; });"
									checked="checked" /></th>
								<th>Nr.</th>
								<th>Name</th>
								<th>Link</th>
							</tr>
						</thead>
						<tbody>
							<%
								Entry[] entrys = ((Playlist)request.getAttribute("playlist")).entrys;
								for (int j = 0;j<entrys.length;j++) {
							%>
							<tr>
								<td><input type="checkbox" name="pimport"
									value="<%=j%>" checked /></td>
								<td><%=j+1%></td>
								<td><%=entrys[j].name%></td>
								<td><a href="<%=entrys[j].link%>"><%=entrys[j].link%></a></td>
							</tr>
							<%
								}
							%>
						</tbody>
					</table>


				</div>
				<input type="submit" name="sub" value="Senden" />
			</form>
		</div>
	</div>
	<%@ include file="footer.jsp"%>