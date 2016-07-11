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
			<div class="bordered gapcloser">
				<div class="headline">Gapcloser - Einstellungen</div>
				<form id="gapcloserform" method="post" action="/gapcloser/">
					<input type="radio" name="mode" value="off" <% if(request.getAttribute("mode") == Gapcloser.Mode.OFF) { %>checked<% } %>> Deaktiviert<br>
					<input type="radio" name="mode" value="zufall100" <% if(request.getAttribute("mode") == Gapcloser.Mode.RANDOM100) { %>checked<% } %>> Zufällig - Top 100<br>
					<input type="radio" name="mode" value="zufall" <% if(request.getAttribute("mode") == Gapcloser.Mode.RANDOM) { %>checked<% } %>> Zufällig - Alles<br>
					<input type="radio" name="mode" value="playlist" <% if(request.getAttribute("mode") == Gapcloser.Mode.PLAYLIST) { %>checked<% } %>> Playlist: <input type="text" name="playlist" value="<%= request.getAttribute("playlist") %>"><br>
					<input type="submit" value="Speichern">
				</form>
			</div>
		</div>
	</div>
	<%@ include file="footer.jsp"%>