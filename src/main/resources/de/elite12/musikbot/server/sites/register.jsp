<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.jsp"%>
<title>Elite12 - Musikbot - Registrierung</title>
</head>
<body>
	<% if(user == null) {%>
	<div id="content">

		<div id="content2">

			<%@ include file="messages.jsp"%>

			<div id="registerform" class="bordered">
				<form method="post" action="/register/">
					<table>
						<tbody>
							<tr>
								<td>Username:</td>
								<td><input type="text" name="username" /></td>
							</tr>
							<tr>
								<td>Password:</td>
								<td><input type="password" name="password" /></td>
							</tr>
							<tr>
								<td>Password bestätigen:</td>
								<td><input type="password" name="password2" /></td>
							</tr>
							<tr>
								<td>Email:</td>
								<td><input type="text" name="mail" /></td>
							</tr>
							<tr>
								<td colspan="2"><input type="checkbox" name="datenschutz">Ich habe die <a href="/impressum/">Datenschutzbestimmungen</a> gelesen und akzeptiert</td>
							</tr>
							<tr>
								<td colspan="2"><input type="submit" name="submit"
									value="Registrieren" /></td>
							</tr>
						</tbody>
					</table>
				</form>
			</div>
		</div>
	</div>
	<% } 
	else {
		response.sendRedirect("/");
	}
	%>
	<%@ include file="footer.jsp"%>