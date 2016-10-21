
<%
	if (request.getAttribute("worked") == null) {
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
			<form id="debug" class="bordered" method="post" action="/debug/">
				<ul>
					<li><input type="submit" name="rclient"
						value="Client Neustarten"></input></li>
					<li><input type="submit" name="rserver"
						value="Server Neustarten"></li>
					</input>
				</ul>
			</form>
		</div>
	</div>
	<%@ include file="footer.jsp"%>