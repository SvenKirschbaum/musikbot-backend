<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.jsp"%>
<title>Elite12 - Musikbot</title>
<script type="text/javascript">
	var updatelog = function ()
	{
		$.post("/log/",function(data) {
			
			$("#log").html(data);
		});
		window.setTimeout(update,6000);
	}
	function start() {
		if(typeof $=='function') {
			updatelog();
		}
		else {
			window.setTimeout(start,1000);
		}
	}
	start();
</script>
</head>
<body>
	<div id="topic"></div>
	<div id="content">

		<div id="content2">

			<%@ include file="messages.jsp"%>
			<div id="log"></div>
		</div>
	</div>
	<%@ include file="footer.jsp"%>