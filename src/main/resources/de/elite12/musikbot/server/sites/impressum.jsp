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
			<div id="impressumtext" class="bordered"><h2>Impressum</h2>
<p>Sven Kirschbaum<br />
Neckarstraße 6<br />41569 Rommerskirchen</p>
<p>Telefon: 01716289040<br />
E-Mail: <a href="mailto:sven@kirschbaum.me">sven@kirschbaum.me</a><br />
</p>
<br /><h2>Disclaimer - rechtliche Hinweise</h2>
§ 1 Haftungsbeschränkung<br />
Die Inhalte dieser Website werden mit größtmöglicher Sorgfalt erstellt. Der
Anbieter übernimmt jedoch keine Gewähr für die Richtigkeit, Vollständigkeit
und Aktualität der bereitgestellten Inhalte. Die Nutzung der Inhalte der
Website erfolgt auf eigene Gefahr des Nutzers. Namentlich gekennzeichnete
Beiträge geben die Meinung des jeweiligen Autors und nicht immer die Meinung
des Anbieters wieder. Mit der reinen Nutzung der Website des Anbieters kommt
keinerlei Vertragsverhältnis zwischen dem Nutzer und dem Anbieter zustande.
<br /><br />
§ 2 Externe Links<br />
Diese Website enthält Verknüpfungen zu Websites Dritter ("externe Links").
Diese Websites unterliegen der Haftung der jeweiligen Betreiber. Der
Anbieter hat bei der erstmaligen Verknüpfung der externen Links die fremden
Inhalte daraufhin überprüft, ob etwaige Rechtsverstöße bestehen. Zu dem
Zeitpunkt waren keine Rechtsverstöße ersichtlich. Der Anbieter hat keinerlei
Einfluss auf die aktuelle und zukünftige Gestaltung und auf die Inhalte der
verknüpften Seiten. Das Setzen von externen Links bedeutet nicht, dass sich
der Anbieter die hinter dem Verweis oder Link liegenden Inhalte zu Eigen
macht. Eine ständige Kontrolle der externen Links ist für den Anbieter ohne
konkrete Hinweise auf Rechtsverstöße nicht zumutbar. Bei Kenntnis von
Rechtsverstößen werden jedoch derartige externe Links unverzüglich gelöscht.
<br /><br />
§ 3 Urheber- und Leistungsschutzrechte<br />
Die auf dieser Website veröffentlichten Inhalte unterliegen dem deutschen
Urheber- und Leistungsschutzrecht. Jede vom deutschen Urheber- und
Leistungsschutzrecht nicht zugelassene Verwertung bedarf der vorherigen
schriftlichen Zustimmung des Anbieters oder jeweiligen Rechteinhabers. Dies
gilt insbesondere für Vervielfältigung, Bearbeitung, Übersetzung,
Einspeicherung, Verarbeitung bzw. Wiedergabe von Inhalten in Datenbanken
oder anderen elektronischen Medien und Systemen. Inhalte und Rechte Dritter
sind dabei als solche gekennzeichnet. Die unerlaubte Vervielfältigung oder
Weitergabe einzelner Inhalte oder kompletter Seiten ist nicht gestattet und
strafbar. Lediglich die Herstellung von Kopien und Downloads für den
persönlichen, privaten und nicht kommerziellen Gebrauch ist erlaubt.
<br /><br />
Die Darstellung dieser Website in fremden Frames ist nur mit schriftlicher
Erlaubnis zulässig.
<br /><br />
§ 4 Besondere Nutzungsbedingungen<br />
Soweit besondere Bedingungen für einzelne Nutzungen dieser Website von den
vorgenannten Paragraphen abweichen, wird an entsprechender Stelle
ausdrücklich darauf hingewiesen. In diesem Falle gelten im jeweiligen
Einzelfall die besonderen Nutzungsbedingungen.<br />Quelle: <a href="http://www.experten-branchenbuch.de/">www.experten-branchenbuch.de</a><br /><br /><h2>Datenschutzerklärung:</h2><p><strong>Datenschutz</strong><br />Nachfolgend möchten wir Sie über unsere Datenschutzerklärung informieren. Sie finden hier Informationen über die Erhebung und Verwendung persönlicher Daten bei der Nutzung unserer Webseite. Wir beachten dabei das für Deutschland geltende Datenschutzrecht. Sie können diese Erklärung jederzeit auf unserer Webseite abrufen. 
<br /><br />
Wir weisen ausdrücklich darauf hin, dass die Datenübertragung im Internet (z.B. bei der Kommunikation per E-Mail) Sicherheitslücken aufweisen und nicht lückenlos vor dem Zugriff durch Dritte geschützt werden kann. 
<br /><br />
Die Verwendung der Kontaktdaten unseres Impressums zur gewerblichen Werbung ist ausdrücklich nicht erwünscht, es sei denn wir hatten zuvor unsere schriftliche Einwilligung erteilt oder es besteht bereits eine Geschäftsbeziehung. Der Anbieter und alle auf dieser Website genannten Personen widersprechen hiermit jeder kommerziellen Verwendung und Weitergabe ihrer Daten.
<br /><br />
<strong>Personenbezogene Daten</strong>
<br />
Sie können unsere Webseite ohne Angabe personenbezogener Daten besuchen. Soweit auf unseren Seiten personenbezogene Daten (wie Name, Anschrift oder E-Mail Adresse) erhoben werden, erfolgt dies, soweit möglich, auf freiwilliger Basis. Diese Daten werden ohne Ihre ausdrückliche Zustimmung nicht an Dritte weitergegeben. Sofern zwischen Ihnen und uns ein Vertragsverhältnis begründet, inhaltlich ausgestaltet oder geändert werden soll oder Sie an uns eine Anfrage stellen, erheben und verwenden wir personenbezogene Daten von Ihnen, soweit dies zu diesen Zwecken erforderlich ist (Bestandsdaten). Wir erheben, verarbeiten und nutzen personenbezogene Daten soweit dies erforderlich ist, um Ihnen die Inanspruchnahme des Webangebots zu ermöglichen (Nutzungsdaten). Sämtliche personenbezogenen Daten werden nur solange gespeichert wie dies für den geannten Zweck (Bearbeitung Ihrer Anfrage oder Abwicklung eines Vertrags) erforderlich ist. Hierbei werden steuer- und handelsrechtliche Aufbewahrungsfristen berücksichtigt. Auf Anordnung der zuständigen Stellen dürfen wir im Einzelfall Auskunft über diese Daten (Bestandsdaten) erteilen, soweit dies für Zwecke der Strafverfolgung, zur Gefahrenabwehr, zur Erfüllung der gesetzlichen Aufgaben der Verfassungsschutzbehörden oder des Militärischen Abschirmdienstes oder zur Durchsetzung der Rechte am geistigen Eigentum erforderlich ist.</p><p><strong>Kommentarfunktionen</strong><br />
Im Rahmen der Kommentarfunktion erheben wir personenbezogene Daten (z.B. Name, E-Mail) im Rahmen Ihrer Kommentierung zu einem Beitrag nur in dem Umfang wie Sie ihn uns mitgeteilt haben. Bei der Veröffentlichung eines Kommentars wird die von Ihnen angegebene Email-Adresse gespeichert, aber nicht veröffentlicht. Ihr Name wird veröffentlich, wenn Sie nicht unter Pseudonym geschrieben haben.</p><p><strong>Datenschutzerklärung für den Webanalysedienst Google Analytics</strong><br />
Diese Website benutzt Google Analytics, einen Webanalysedienst der Google Inc. ("Google"). Google Analytics verwendet sog. "Cookies", Textdateien, die auf Ihrem Computer gespeichert werden und die eine Analyse der Benutzung der Website durch Sie ermöglichen. Die durch den Cookie erzeugten Informationen über Ihre Benutzung dieser Website werden in der Regel an einen Server von Google in den USA übertragen und dort gespeichert. Wir haben die IP-Anonymisierung aktiviert. Auf dieser Webseite wird Ihre IP-Adresse von Google daher innerhalb von Mitgliedstaaten der Europäischen Union oder in anderen Vertragsstaaten des Abkommens über den Europäischen Wirtschaftsraum zuvor gekürzt. Nur in Ausnahmefällen wird die volle IP-Adresse an einen Server von Google in den USA übertragen und dort gekürzt. Im Auftrag des Betreibers dieser Website wird Google diese Informationen benutzen, um Ihre Nutzung der Website auszuwerten, um Reports über die Websiteaktivitäten zusammenzustellen und um weitere mit der Websitenutzung und der Internetnutzung verbundene Dienstleistungen gegenüber dem Websitebetreiber zu erbringen. Die im Rahmen von Google Analytics von Ihrem Browser übermittelte IP-Adresse wird nicht mit anderen Daten von Google zusammengeführt. Sie können die Speicherung der Cookies durch eine entsprechende Einstellung Ihrer Browser-Software verhindern; wir weisen Sie jedoch darauf hin, dass Sie in diesem Fall gegebenenfalls nicht sämtliche Funktionen dieser Website vollumfänglich werden nutzen können. Sie können darüber hinaus die Erfassung der durch das Cookie erzeugten und auf Ihre Nutzung der Website bezogenen Daten (inkl. Ihrer IP-Adresse) an Google sowie die Verarbeitung dieser Daten durch Google verhindern, indem sie das unter dem folgenden Link verfügbare Browser-Plugin herunterladen und installieren: <a href="http://tools.google.com/dlpage/gaoptout?hl=de" rel="nofollow">http://tools.google.com/dlpage/gaoptout?hl=de</a></p><p><strong>Datenschutzerklärung für die "Google Remarketing" und "Ähnliche Zielgruppen"-Funktion der Google Inc.</strong><br />    
Diese Website verwendet die Remarketing- bzw. "Ähnliche Zielgruppen"-Funktion der Google Inc., 1600 Amphitheatre Parkway, Mountain View, CA 94043, United States ("Google"). Sie können so zielgerichtet mit Werbung angesprochen werden, indem personalisierte und interessenbezogene Anzeigen geschaltet werden, wenn Sie andere Webseiten im sog. "Google Display-Netzwerk" besuchen. "Google Remarketing" bzw. die Funktion "Ähnliche Zielgruppen" verwendet dafür sog. "Cookies", Textdateien, die auf Ihrem Computer gespeichert werden und die eine Analyse der Benutzung der Website durch Sie ermöglichen. Über diese Textdateien werden Ihre Besuche sowie anonymisierte Daten über die Nutzung der Website erfasst. Personenbezogene Daten werden dabei nicht gespeichert. Besuchen Sie eine andere Webseite im sog. "Google Display-Netzwerk" werden Ihnen ggf. Werbeeinblendungen angezeigt, die mit hoher Wahrscheinlichkeit zuvor auf unserer Website aufgerufene Produkt- und Informationsbereiche berücksichtigen.<br />
Sie können das "Google Remarketing" bzw. die "Ähnliche Zielgruppen"-Funktion verhindern, indem Sie die Speicherung der Cookies durch eine entsprechende Einstellung Ihrer Browser-Software unterbinden. Wir weisen Sie jedoch darauf hin, dass Sie in diesem Fall gegebenenfalls nicht sämtliche Funktionen dieser Website vollumfänglich werden nutzen können. Sie können darüber hinaus die Erfassung der durch das Cookie erzeugten und auf Ihre Nutzung der Website bezogenen Daten an Google sowie die Verarbeitung dieser Daten durch Google verhindern, indem sie das unter dem folgenden Link verfügbare Browser-Plugin herunterladen und installieren: <a href="https://www.google.com/settings/ads/plugin?hl=de" target="_blank">https://www.google.com/settings/ads/plugin?hl=de</a>. Sie können zudem die Verwendung von Cookies durch Drittanbieter deaktivieren, indem sie die Deaktivierungsseite der Netzwerkwerbeinitiative (Network Advertising Initiative) unter <a href="http://www.networkadvertising.org/choices/" target="_blank">http://www.networkadvertising.org/choices/</a> aufrufen und die dort genannten weiterführenden Information zum Opt-Out umsetzen. Die Datenschutzerklärung von Google zum Remarketing mit weiteren Informationen finden Sie hier: <a href="http://www.google.com/privacy/ads/" target="_blank">http://www.google.com/privacy/ads/</a>.
</p><p><strong>Auskunftsrecht</strong><br />Sie haben das jederzeitige Recht, sich unentgeltlich und unverzüglich über die zu Ihrer Person erhobenen Daten zu erkundigen. Sie haben das jederzeitige Recht, Ihre Zustimmung zur Verwendung Ihrer angegeben persönlichen Daten mit Wirkung für die Zukunft zu widerrufen. Zur Auskunftserteilung wenden Sie sich bitte an den Anbieter unter den Kontaktdaten im Impressum.</p></div>
		</div>
	</div>
	<%@ include file="footer.jsp"%>