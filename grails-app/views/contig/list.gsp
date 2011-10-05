<%@ page import="afterparty.Contig" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'contig.label', default: 'Contig')}"/>
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

                <g:sortableColumn property="id" title="${message(code: 'contig.id.label', default: 'Id')}"/>

                <g:sortableColumn property="name" title="${message(code: 'contig.name.label', default: 'Name')}"/>

                <g:sortableColumn property="sequence"
                                  title="${message(code: 'contig.sequence.label', default: 'Sequence')}"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${contigInstanceList}" status="i" var="contigInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show"
                                id="${contigInstance.id}">${fieldValue(bean: contigInstance, field: "id")}</g:link></td>

                    <td>${fieldValue(bean: contigInstance, field: "name")}</td>

                    <td>${fieldValue(bean: contigInstance, field: "sequence")}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${contigInstanceTotal}"/>
    </div>
</div>
</body>
</html>
