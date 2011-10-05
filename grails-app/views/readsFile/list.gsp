<%@ page import="afterparty.ReadsFile" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp.bak"/>
    <g:set var="entityName" value="${message(code: 'readsFile.label', default: 'ReadsFile')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                               args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="${message(code: 'readsFile.id.label', default: 'Id')}"/>

                <g:sortableColumn property="name" title="${message(code: 'readsFile.name.label', default: 'Name')}"/>

                <g:sortableColumn property="description"
                                  title="${message(code: 'readsFile.description.label', default: 'Description')}"/>

                <g:sortableColumn property="fastqFile"
                                  title="${message(code: 'readsFile.fastqFile.label', default: 'Fastq File')}"/>

                <th><g:message code="readsFile.run.label" default="Run"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${readsFileInstanceList}" status="i" var="readsFileInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show"
                                id="${readsFileInstance.id}">${fieldValue(bean: readsFileInstance, field: "id")}</g:link></td>

                    <td>${fieldValue(bean: readsFileInstance, field: "name")}</td>

                    <td>${fieldValue(bean: readsFileInstance, field: "description")}</td>



                    <td>${fieldValue(bean: readsFileInstance, field: "run")}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${readsFileInstanceTotal}"/>
    </div>
</div>
</body>
</html>
