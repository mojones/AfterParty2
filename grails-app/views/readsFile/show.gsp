<%@ page import="afterparty.ReadsFile" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp.bak"/>
    <g:set var="entityName" value="${message(code: 'readsFile.label', default: 'ReadsFile')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label"
                                                                           args="[entityName]"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                               args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.show.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="readsFile.id.label" default="Id"/></td>

                <td valign="top" class="value">${fieldValue(bean: readsFileInstance, field: "id")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="readsFile.name.label" default="Name"/></td>

                <td valign="top" class="value">${fieldValue(bean: readsFileInstance, field: "name")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="readsFile.description.label" default="Description"/></td>

                <td valign="top" class="value">${fieldValue(bean: readsFileInstance, field: "description")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="readsFile.fastqFile.label" default="Fastq File"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="readsFile.run.label" default="Run"/></td>

                <td valign="top" class="value"><g:link controller="run" action="show"
                                                       id="${readsFileInstance?.run?.id}">${readsFileInstance?.run?.encodeAsHTML()}</g:link></td>

            </tr>

            </tbody>
        </table>
    </div>
    <object data="<g:createLink action="graph" params="[id : readsFileInstance.id]"/> " type="image/svg+xml"  width="1000" height="1000" id="mySVG" />

</div>
</body>
</html>
