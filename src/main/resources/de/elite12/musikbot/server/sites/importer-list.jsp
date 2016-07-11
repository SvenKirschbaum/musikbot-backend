<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="com.google.api.services.youtube.model.Playlist"%>
<%@page import="com.google.api.services.youtube.model.PlaylistItem"%>
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
							href="http://www.youtube.com/playlist?list=<%=((Playlist) request.getAttribute("playlistmeta"))
					.getId()%>"><%=((Playlist) request.getAttribute("playlistmeta"))
					.getSnippet().getTitle()%></a>
					</div>

					<input type="hidden" name="import" value="finish" /> <input
						type="hidden" name="playlist"
						value="http://www.youtube.com/playlist?list=<%=((Playlist) request.getAttribute("playlistmeta"))
					.getId()%>" />
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
								List<PlaylistItem> list = (List<PlaylistItem>) request.getAttribute("playlist");
								for (PlaylistItem entry : list) {
							%>
							<tr>
								<td><input type="checkbox" name="pimport"
									value="<%=entry.getSnippet().getPosition()%>" checked /></td>
								<td><%=entry.getSnippet().getPosition()+1%></td>
								<td><%=entry.getSnippet().getTitle()%></td>
								<td><a href="https://www.youtube.com/watch?v=<%=entry.getSnippet().getResourceId().getVideoId()%>">https://www.youtube.com/watch?v=<%=entry.getSnippet().getResourceId().getVideoId()%></a></td>
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