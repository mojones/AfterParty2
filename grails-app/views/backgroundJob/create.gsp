<%@ page import="afterparty.BackgroundJob" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp.bak"/>
    <g:set var="entityName" value="${message(code: 'backgroundJob.label', default: 'BackgroundJob')}"/>
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
    <g:hasErrors bean="${backgroundJobInstance}">
        <div class="errors">
            <g:renderErrors bean="${backgroundJobInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><g:message code="backgroundJob.name.label" default="Name"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: backgroundJobInstance, field: 'name', 'errors')}">
                        <g:textField name="name" value="${backgroundJobInstance?.name}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="progress"><g:message code="backgroundJob.progress.label"
                                                         default="Progress"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: backgroundJobInstance, field: 'progress', 'errors')}">
                        <g:textField name="progress" value="${backgroundJobInstance?.progress}"/>
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
