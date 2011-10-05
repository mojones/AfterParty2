<%@ page import="afterparty.ReadsFile" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp.bak"/>
    <g:set var="entityName" value="${message(code: 'readsFile.label', default: 'ReadsFile')}"/>
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
    <g:hasErrors bean="${readsFileInstance}">
        <div class="errors">
            <g:renderErrors bean="${readsFileInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" enctype="multipart/form-data">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><g:message code="readsFile.name.label" default="Name"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: readsFileInstance, field: 'name', 'errors')}">
                        <g:textArea name="name" cols="40" rows="5" value="${readsFileInstance?.name}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><g:message code="readsFile.description.label"
                                                            default="Description"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: readsFileInstance, field: 'description', 'errors')}">
                        <g:textArea name="description" cols="40" rows="5" value="${readsFileInstance?.description}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="fastqFile"><g:message code="readsFile.fastqFile.label"
                                                          default="Fastq File"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: readsFileInstance, field: 'fastqFile', 'errors')}">
                        <input type="file" id="fastqFile" name="fastqFile"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="run"><g:message code="readsFile.run.label" default="Run"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: readsFileInstance, field: 'run', 'errors')}">
                        <g:select name="run.id" from="${afterparty.Run.list()}" optionKey="id"
                                  value="${readsFileInstance?.run?.id}"/>
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
