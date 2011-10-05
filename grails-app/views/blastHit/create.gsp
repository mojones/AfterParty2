<%@ page import="afterparty.BlastHit" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp.bak"/>
    <g:set var="entityName" value="${message(code: 'blastHit.label', default: 'BlastHit')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label"
                                                                           args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.create.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${blastHitInstance}">
        <div class="errors">
            <g:renderErrors bean="${blastHitInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><g:message code="blastHit.description.label"
                                                            default="Description"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: blastHitInstance, field: 'description', 'errors')}">
                        <g:textArea name="description" cols="40" rows="5" value="${blastHitInstance?.description}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="accession"><g:message code="blastHit.accession.label" default="Accession"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: blastHitInstance, field: 'accession', 'errors')}">
                        <g:textField name="accession" value="${blastHitInstance?.accession}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="bitscore"><g:message code="blastHit.bitscore.label" default="Bitscore"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: blastHitInstance, field: 'bitscore', 'errors')}">
                        <g:textField name="bitscore" value="${fieldValue(bean: blastHitInstance, field: 'bitscore')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="contig"><g:message code="blastHit.contig.label" default="Contig"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: blastHitInstance, field: 'contig', 'errors')}">
                        <g:select name="contig.id" from="${afterparty.Contig.list()}" optionKey="id"
                                  value="${blastHitInstance?.contig?.id}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:submitButton name="create" class="save"
                                                 value="${message(code: 'default.button.create.label', default: 'Create')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
