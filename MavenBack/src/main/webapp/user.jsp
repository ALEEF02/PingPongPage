<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title><%= (String)request.getAttribute("reqUsername") %></title>
	</head>
	<body>
		<h1 id="uname"><%= (String)request.getAttribute("reqUsername") %></h1>
		<h2>Elo:</h2>
		<p id="elo"><%= (String)request.getAttribute("reqElo") %></p>
		<h2>Join Date:</h2>
		<p id="jd"></p>
		<h2>Last Login:</h2>
		<p id="ld"></p>
	</body>
</html>