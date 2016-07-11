<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.jsp"%>
<title>Elite12 - Musikbot - Wer ist online?</title>
</head>
<body>
	<%
		ResultSet rs = (ResultSet) request.getAttribute("result");
	%>
	<div id="topic"></div>
	<div id="content">

		<div id="content2">

			<%@ include file="messages.jsp"%>
		</div>
		<div id="onlinelist" class="bordered">
			<ul>
				<% while(rs.next()) { %>
				<li><%= rs.getString("NAME") %></li>
				<% } %>
			</ul>
		</div>
	</div>
	<%@ include file="footer.jsp"%>