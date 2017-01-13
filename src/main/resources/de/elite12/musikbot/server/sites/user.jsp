
<%
	if (request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.jsp"%>
<title>USERNAME - Elite12 - Musikbot</title>
</head>
<body>
	<div id="topic"></div>

	<div id="usercontainer">
		<div id="pbild" class="bordered">
			<img alt="profilbild"
				src="https://www.gravatar.com/avatar/<%=Util.md5Hex(((User) request.getAttribute("user")).getEmail().toLowerCase(Locale.GERMAN))%>?s=350" />
		</div>
		<div id="userstats" class="bordered"></div>
		<div id="topsongs" class="bordered"></div>
	</div>
	<%@ include file="footer.jsp"%>