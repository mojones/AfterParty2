<%@ page import="afterparty.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <title>Dataset List</title>
</head>

<body>
<div class="row-fluid">
    <div class="span10 offset1">
        <g:each in="${studyInstanceList}" status="i" var="studyInstance">
            <div class='in_a_box study_box'>
		<h2><g:link action="show" id="${studyInstance.id}"><i class="icon-th-list"></i>&nbsp;${studyInstance.name}</g:link></h2>
		<p>${studyInstance.description}</p>
		</div>	
        </g:each>
    </div>        
</div>        
</body>
</html>
