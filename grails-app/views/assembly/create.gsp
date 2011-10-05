<%@ page import="afterparty.Assembly" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp.bak"/>
    <g:set var="entityName" value="${message(code: 'assembly.label', default: 'Assembly')}"/>
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
    <g:hasErrors bean="${assemblyInstance}">
        <div class="errors">
            <g:renderErrors bean="${assemblyInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><g:message code="assembly.description.label"
                                                            default="Description"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: assemblyInstance, field: 'description', 'errors')}">
                        <g:textField name="description" value="${assemblyInstance?.description}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="study"><g:message code="assembly.study.label" default="Study"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: assemblyInstance, field: 'study', 'errors')}">
                        <g:select name="study.id" from="${afterparty.Study.list()}" optionKey="id"
                                  value="${assemblyInstance?.study?.id}"/>
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
