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
			<form id="import" class="bordered" method="post" action="/import/">
				Playlist: <input type="text" name="playlist" /><input id=""
					type="submit" name="sub" />
			</form>
		</div>
	</div>
	<%@ include file="footer.jsp"%>