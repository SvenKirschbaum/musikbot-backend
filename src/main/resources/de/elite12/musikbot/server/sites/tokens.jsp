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
			<form id="tokenform" class="bordered" method="POST">
				<input type="text" name="token" disabled value="<%= request.getAttribute("token") %>" />
				<input type="submit" value="Token resetten" />
			</form>
		</div>
	</div>
	<%@ include file="footer.jsp"%>