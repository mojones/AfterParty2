<%@ page import="afterparty.BlastHit" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp.bak"/>
    <g:set var="entityName" value="${message(code: 'blastHit.label', default: 'BlastHit')}"/>
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

                <g:sortableColumn property="id" title="${message(code: 'blastHit.id.label', default: 'Id')}"/>

                <g:sortableColumn property="description"
                                  title="${message(code: 'blastHit.description.label', default: 'Description')}"/>

                <g:sortableColumn property="accession"
                                  title="${message(code: 'blastHit.accession.label', default: 'Accession')}"/>

                <g:sortableColumn property="bitscore"
                                  title="${message(code: 'blastHit.bitscore.label', default: 'Bitscore')}"/>

                <th><g:message code="blastHit.contig.label" default="Contig"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${blastHitInstanceList}" status="i" var="blastHitInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show"
                                id="${blastHitInstance.id}">${fieldValue(bean: blastHitInstance, field: "id")}</g:link></td>

                    <td>${fieldValue(bean: blastHitInstance, field: "description")}</td>

                    <td>${fieldValue(bean: blastHitInstance, field: "accession")}</td>

                    <td>${fieldValue(bean: blastHitInstance, field: "bitscore")}</td>

                    <td>${fieldValue(bean: blastHitInstance, field: "contig")}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${blastHitInstanceTotal}"/>
    </div>
</div>
</body>
</html>
