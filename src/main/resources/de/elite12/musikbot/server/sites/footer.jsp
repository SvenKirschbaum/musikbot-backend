<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.*"%>
<%@ page import="java.net.URL"%>
<%@ page import="sun.net.www.content.text.PlainTextInputStream"%>
<%@ page import="java.io.InputStream"%>
<%@ page import="de.elite12.musikbot.shared.Util"%>

<%
	if (request.getAttribute("worked") == null) {
		response.sendRedirect("/");
		return;
	}
	SimpleDateFormat time = new SimpleDateFormat("H:mm");
%>
<%@ page import="de.elite12.musikbot.server.*"%>
<div id="footer">
	<%
		if (session.getAttribute("user") == null) {
	%>
	<div id="login">
		<span onclick="show(loginbox)" class="link">Login</span> <span
			class="link"><a href="/register/">Registrieren</a></span>
		<div id="loginbox">
			<form method="post" action="/">
				<input type="hidden" name="action" value="login" />
				<table>
					<tbody>
						<tr>
							<td colspan="2"></td>
							<td onclick="hide(loginbox);" onmouseover="pointer();"
								onmouseout="returncursor();">x</td>
						</tr>
						<tr>
							<td>Username:</td>
							<td><input type="text" name="user" /></td>
							<td></td>

						</tr>
						<tr>
							<td>Passwort:</td>
							<td><input type="password" name="password" /></td>
							<td></td>

						</tr>
						<tr>

							<td></td>
							<td><input class="button" type="submit" value="Einloggen" />

							</td>
							<td></td>


						</tr>
					</tbody>
				</table>
			</form>
		</div>
	</div>
	<%
		} else {
	%>
	<div id="login">
		<div id="profilbild" class="tooltip">
			<img alt="profilbild" src="https://www.gravatar.com/avatar/<%= Util.md5Hex(((User)session.getAttribute("user")).getEmail().toLowerCase(Locale.GERMAN)) %>?s=20" />
			<div class="bordered"><img alt="profilbild" src="https://www.gravatar.com/avatar/<%= Util.md5Hex(((User)session.getAttribute("user")).getEmail().toLowerCase(Locale.GERMAN)) %>?s=350" /></div>
		</div>
		<form id="logoutform" action="/" method="post">
			<input type="hidden" name="action" value="logout" />
		</form>
		<div class="userbar">
			Willkommen
			<%=((User) session.getAttribute("user")).getName()%>! <span
				onclick="document.getElementById('logoutform').submit()"
				class="link">(Logout)</span>
				
		</div>
	</div>

			<%
				if (((User) session.getAttribute("user")).isAdmin()) {
			%>
			<div id="amenu">
				<span class="link" onclick="togglevis(amenubox)">Admin-Menü</span>
				<div id="amenubox">
					<ul>
						<li><a href="/">Startseite</a></li>
						<li><a href="/archiv/">Archiv</a></li>
						<li><a href="/statistik/">Statistik</a></li>
						<li><a href="/import/">Playlist Importieren</a></li>
						<li><a href="/songs/">Gesperrte Songs</a></li>
						<li><a href="/gapcloser/">Gapcloser</a></li>
						<li><a href="/log/">Log</a></li>
						<li><a href="/whoisonline/">Wer ist online?</a></li>
					</ul>
				</div>
			</div>
	<%
				}
		}
	%>
	
	<div id="styleselect">
		<span class="link" onclick="togglevis(styleselectbox)">Style ändern</span>
		<div id="styleselectbox">
			<ul>
				<li><a href="/setstyle/radio">radio</a></li>
			</ul>
		</div>
	</div>
	
	<span id="stats" class="link"><a href="/statistik/">Statistik</a></span>
	
	<span id="impressum" class="link"><a href="/impressum/">Impressum/Disclaimer/Datenschutz</a></span>

	<div id="time"><%=time.format(new java.util.Date())%></div>

	<div id="branding">
	</div>

	<div id="spotify">
		<svg version="1.1" id="Layer_2" xmlns="http://www.w3.org/2000/svg"
			xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
			width="662.242px" height="198.624px" viewbox="0 0 662.242 198.624"
			enable-background="new 0 0 662.242 198.624" xml:space="preserve">
			<g>
				<g>
					<g>
						<g>
							<g>
								<lineargradient id="SVGID_1_" gradientunits="userSpaceOnUse"
				x1="99.312" y1="0.6992" x2="99.312" y2="199.0151">
									<stop offset="0" style="stop-color:#84BD00" />
									<stop offset="1" style="stop-color:#7DAE00" />
								</lineargradient>
								<path fill="url(#SVGID_1_)"
				d="M99.312,0C44.464,0,0,44.463,0,99.311c0,54.853,44.464,99.313,99.312,99.313
									c54.85,0,99.312-44.461,99.312-99.313C198.624,44.463,154.162,0,99.312,0z" />
							</g>
							<g opacity="0.4">
								
									<radialgradient id="SVGID_2_" cx="99.312" cy="99.312"
				r="135.7506" fx="99.4958" fy="33.5095"
				gradientunits="userSpaceOnUse">
									<stop offset="0.0919" style="stop-color:#FFFFFF" />
									<stop offset="0.8336" style="stop-color:#FFFFFF;stop-opacity:0" />
								</radialgradient>
								<path fill="url(#SVGID_2_)"
				d="M99.312,0C44.464,0,0,44.463,0,99.311c0,54.853,44.464,99.313,99.312,99.313
									c54.85,0,99.312-44.461,99.312-99.313C198.624,44.463,154.162,0,99.312,0z" />
							</g>
							<g opacity="0.2">
								<g>
									<path
				d="M42.899,92.362c36.07-10.945,80.91-5.644,111.563,13.194c2.22,1.37,3.497,3.674,3.658,6.091
										c0.047-2.646-1.239-5.256-3.658-6.745c-30.654-18.838-75.494-24.14-111.563-13.193c-3.586,1.089-5.799,4.545-5.458,8.141
										C37.411,96.498,39.533,93.386,42.899,92.362z" />
									<path
				d="M46.347,123.661c37.83-8.648,70.281-4.927,96.458,11.069c1.769,1.079,2.79,2.909,2.933,4.831
										c0.068-2.155-0.961-4.282-2.933-5.485c-26.177-15.996-58.627-19.719-96.458-11.07c-3.004,0.685-4.97,3.441-4.782,6.415
										C41.687,126.706,43.565,124.292,46.347,123.661z" />
									<path
				d="M37.289,58.781c35.081-10.649,93.397-8.592,130.25,13.285c2.719,1.612,4.299,4.395,4.513,7.324
										c-0.006-3.162-1.593-6.246-4.513-7.979c-36.854-21.876-95.17-23.933-130.25-13.285c-4.305,1.309-6.96,5.453-6.549,9.762
										C30.644,63.824,33.205,60.022,37.289,58.781z" />
								</g>
							</g>
							<g>
								<path
				d="M154.462,105.556C123.809,86.718,78.969,81.417,42.899,92.362c-4.087,1.245-6.397,5.562-5.163,9.657
									c1.244,4.087,5.568,6.396,9.663,5.158c31.575-9.584,72.272-4.832,98.96,11.573c3.64,2.237,8.405,1.095,10.65-2.55
									C159.248,112.564,158.102,107.797,154.462,105.556z" />
								<path
				d="M167.54,72.066c-36.854-21.876-95.17-23.934-130.25-13.285c-4.911,1.492-7.679,6.678-6.191,11.588
									c1.487,4.907,6.676,7.678,11.584,6.189c30.56-9.275,83.364-7.527,115.376,11.484c4.403,2.621,10.108,1.166,12.727-3.248
									C173.402,80.387,171.954,74.686,167.54,72.066z" />
								<path
				d="M142.804,134.73c-26.177-15.996-58.627-19.718-96.458-11.069c-3.339,0.76-5.418,4.08-4.655,7.411
									c0.76,3.33,4.08,5.418,7.411,4.654c34.569-7.896,63.923-4.678,87.244,9.576c2.916,1.783,6.727,0.866,8.511-2.06
									C146.642,140.325,145.723,136.513,142.804,134.73z" />
							</g>
						</g>
					</g>
				</g>
				<g>
					<g>
						<path fill="#FFFFFF"
				d="M270.193,91.682c-17.146-4.089-20.2-6.959-20.2-12.989c0-5.698,5.364-9.531,13.341-9.531
							c7.732,0,15.398,2.912,23.438,8.905c0.243,0.181,0.548,0.254,0.848,0.207c0.3-0.045,0.565-0.209,0.741-0.458l8.373-11.802
							c0.344-0.486,0.25-1.156-0.214-1.527c-9.566-7.677-20.34-11.409-32.932-11.409c-18.515,0-31.446,11.11-31.446,27.008
							c0,17.048,11.155,23.083,30.434,27.743c16.408,3.779,19.178,6.945,19.178,12.606c0,6.272-5.6,10.172-14.611,10.172
							c-10.009,0-18.173-3.372-27.306-11.28c-0.227-0.195-0.537-0.285-0.824-0.269c-0.302,0.025-0.578,0.165-0.771,0.396l-9.388,11.172
							c-0.394,0.464-0.344,1.156,0.111,1.558c10.626,9.486,23.694,14.497,37.797,14.497c19.951,0,32.843-10.901,32.843-27.773
							C299.604,104.649,291.085,96.763,270.193,91.682z" />
						<path fill="#FFFFFF"
				d="M344.741,74.771c-8.647,0-15.74,3.406-21.59,10.385V77.3c0-0.62-0.503-1.125-1.122-1.125h-15.354
							c-0.62,0-1.122,0.505-1.122,1.125v87.286c0,0.62,0.502,1.125,1.122,1.125h15.354c0.619,0,1.122-0.505,1.122-1.125v-27.552
							c5.851,6.565,12.944,9.772,21.59,9.772c16.068,0,32.334-12.369,32.334-36.014C377.075,87.143,360.81,74.771,344.741,74.771z
							 M359.224,110.793c0,12.04-7.417,20.442-18.036,20.442c-10.499,0-18.419-8.784-18.419-20.442c0-11.657,7.92-20.442,18.419-20.442
							C351.636,90.351,359.224,98.946,359.224,110.793z" />
						<path fill="#FFFFFF"
				d="M418.77,74.771c-20.693,0-36.904,15.934-36.904,36.279c0,20.124,16.1,35.891,36.65,35.891
							c20.767,0,37.027-15.881,37.027-36.147C455.543,90.593,439.394,74.771,418.77,74.771z M418.77,131.361
							c-11.007,0-19.305-8.844-19.305-20.568c0-11.774,8.011-20.319,19.051-20.319c11.077,0,19.432,8.843,19.432,20.576
							C437.947,122.822,429.882,131.361,418.77,131.361z" />
						<path fill="#FFFFFF"
				d="M499.732,76.175h-16.896V58.901c0-0.62-0.501-1.125-1.121-1.125h-15.352
							c-0.621,0-1.126,0.505-1.126,1.125v17.274h-7.383c-0.618,0-1.118,0.505-1.118,1.125v13.195c0,0.62,0.5,1.125,1.118,1.125h7.383
							v34.144c0,13.798,6.867,20.794,20.412,20.794c5.507,0,10.076-1.137,14.383-3.579c0.35-0.195,0.567-0.572,0.567-0.974V129.44
							c0-0.388-0.202-0.754-0.533-0.957c-0.335-0.21-0.751-0.221-1.094-0.051c-2.958,1.488-5.817,2.175-9.013,2.175
							c-4.926,0-7.124-2.236-7.124-7.249V91.621h16.896c0.62,0,1.12-0.505,1.12-1.125V77.3
							C500.853,76.681,500.353,76.175,499.732,76.175z" />
						<path fill="#FFFFFF"
				d="M558.598,76.242v-2.122c0-6.242,2.393-9.025,7.761-9.025c3.2,0,5.771,0.636,8.651,1.597
							c0.354,0.111,0.725,0.056,1.013-0.156c0.296-0.212,0.464-0.553,0.464-0.913V52.685c0-0.494-0.318-0.933-0.794-1.078
							c-3.042-0.905-6.934-1.834-12.762-1.834c-14.182,0-21.677,7.986-21.677,23.086v3.25h-7.376c-0.619,0-1.127,0.505-1.127,1.125
							v13.263c0,0.62,0.508,1.125,1.127,1.125h7.376v52.666c0,0.62,0.5,1.125,1.119,1.125h15.354c0.62,0,1.125-0.505,1.125-1.125
							V91.621h14.335l21.96,52.652c-2.493,5.532-4.944,6.633-8.291,6.633c-2.705,0-5.554-0.808-8.466-2.401
							c-0.274-0.15-0.599-0.176-0.895-0.084c-0.293,0.104-0.54,0.321-0.664,0.606l-5.204,11.417c-0.248,0.539-0.036,1.173,0.483,1.452
							c5.433,2.942,10.337,4.198,16.397,4.198c11.337,0,17.604-5.281,23.128-19.488l26.637-68.831c0.134-0.346,0.094-0.737-0.118-1.044
							c-0.211-0.304-0.552-0.488-0.924-0.488h-15.983c-0.479,0-0.907,0.304-1.063,0.754l-16.374,46.77l-17.935-46.8
							c-0.164-0.436-0.583-0.723-1.049-0.723H558.598z" />
						<path fill="#FFFFFF"
				d="M524.472,76.175h-15.354c-0.62,0-1.125,0.505-1.125,1.125v66.986c0,0.62,0.505,1.125,1.125,1.125h15.354
							c0.619,0,1.125-0.505,1.125-1.125V77.3C525.597,76.681,525.091,76.175,524.472,76.175z" />
						<path fill="#FFFFFF"
				d="M516.873,45.675c-6.082,0-11.019,4.924-11.019,11.007c0,6.086,4.937,11.016,11.019,11.016
							c6.08,0,11.011-4.93,11.011-11.016C527.884,50.599,522.953,45.675,516.873,45.675z" />
					</g>
					<g>
						<path fill="#FFFFFF"
				d="M651.381,97.712c-6.076,0-10.805-4.88-10.805-10.806s4.79-10.862,10.861-10.862
							c6.076,0,10.805,4.879,10.805,10.8C662.242,92.771,657.456,97.712,651.381,97.712z M651.438,77.119
							c-5.534,0-9.723,4.399-9.723,9.787c0,5.385,4.158,9.726,9.666,9.726c5.533,0,9.726-4.396,9.726-9.787
							C661.106,81.459,656.945,77.119,651.438,77.119z M653.833,87.956l3.057,4.279h-2.577l-2.751-3.925h-2.365v3.925h-2.157V80.893
							h5.058c2.635,0,4.368,1.348,4.368,3.618C656.465,86.37,655.391,87.506,653.833,87.956z M652.01,82.838h-2.813v3.587h2.813
							c1.404,0,2.242-0.687,2.242-1.795C654.252,83.464,653.414,82.838,652.01,82.838z" />
					</g>
				</g>
			</g>
		</svg>

	</div>
	<div id="html5">
		<svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg"
			xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
			width="512px" height="512px" viewbox="0 0 512 512"
			enable-background="new 0 0 512 512" xml:space="preserve">
			<g>
				<g>
					<path fill="#FFFFFF"
				d="M119.387,20.312h21.298v21.045h19.485V20.312h21.303v63.727H160.17V62.7h-19.485v21.338h-21.298V20.312z" />
					<path fill="#FFFFFF"
				d="M209.482,41.444h-18.754V20.312h58.812v21.133h-18.759v42.594h-21.3V41.444z" />
					<path fill="#FFFFFF"
				d="M258.879,20.312h22.21l13.661,22.392l13.648-22.392h22.219v63.727h-21.212V52.453L294.75,75.111h-0.366
						l-14.665-22.658v31.585h-20.84V20.312z" />
					<path fill="#FFFFFF"
				d="M341.219,20.312h21.308v42.664h29.955v21.062h-51.263V20.312z" />
				</g>
				<path fill="#FFFFFF"
				d="M200.662,266.676H256v-42.92h-59.169L200.662,266.676z M88.686,111.982l30.47,341.74l136.762,37.966
					l136.891-37.948l30.507-341.758H88.686z M366.694,431.981L256,462.668v-43.494l-0.067,0.02l-85.858-23.835l-6.004-67.298h42.075
					l3.116,34.914l46.68,12.607l0.059-0.019V308.59h-93.669l-11.306-126.749H256v-41.914h136.766L366.694,431.981z" />
				<path opacity="0.8" fill="#FFFFFF"
				d="M307.592,308.59H256v66.974l46.728-12.613L307.592,308.59z M256,139.927v41.914h104.975
					l-3.754,41.915H256v42.92h97.406l-11.499,128.683L256,419.174v43.494l110.694-30.687l26.071-292.055H256z" />
				<g opacity="0.2">
					<polygon fill="#FFFFFF"
				points="256,181.841 151.025,181.841 162.331,308.59 256,308.59 256,266.676 200.662,266.676 
						196.831,223.756 256,223.756 		" />
					<polygon fill="#FFFFFF"
				points="256,375.563 255.941,375.582 209.262,362.975 206.146,328.061 164.07,328.061 170.074,395.358 
						255.933,419.193 256,419.174 		" />
				</g>
			</g>
		</svg>
	</div>
	<div class="version">v<%= ((Controller)request.getAttribute("control")).version %></div>
</div>
</body>
</html>
