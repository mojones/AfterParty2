<%@ page import="afterparty.BackgroundJob" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp.bak"/>
    <g:set var="entityName" value="${message(code: 'backgroundJob.label', default: 'BackgroundJob')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<div>
    <p>
        <b>Name:</b> ${backgroundJobInstance.name}<br/>
        <b>Progress:</b> ${backgroundJobInstance.progress}<br/>
        <b>Command line:</b> ${backgroundJobInstance.commandLine}<br/>
        <object data="<g:createLink controller="backgroundJob" action="graph" params="[id : backgroundJobInstance.id]"/> " type="image/svg+xml"  width="1000" height="1000" id="mySVG" />

    </p>

</div>

</body>
</html>
