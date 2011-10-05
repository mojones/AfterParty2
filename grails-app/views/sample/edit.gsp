<%@ page import="afterparty.Sample" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp.bak"/>
    <g:set var="entityName" value="${message(code: 'sample.label', default: 'Sample')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
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
    <h1><g:message code="default.edit.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${sampleInstance}">
        <div class="errors">
            <g:renderErrors bean="${sampleInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <g:hiddenField name="id" value="${sampleInstance?.id}"/>
        <g:hiddenField name="version" value="${sampleInstance?.version}"/>
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><g:message code="sample.name.label" default="Name"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'name', 'errors')}">
                        <g:textArea name="name" cols="40" rows="5" value="${sampleInstance?.name}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><g:message code="sample.description.label"
                                                            default="Description"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'description', 'errors')}">
                        <g:textArea name="description" cols="40" rows="5" value="${sampleInstance?.description}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="experiments"><g:message code="sample.experiments.label"
                                                            default="Experiments"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'experiments', 'errors')}">

                        <ul>
                            <g:each in="${sampleInstance?.experiments?}" var="e">
                                <li><g:link controller="experiment" action="show"
                                            id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
                            </g:each>
                        </ul>
                        <g:link controller="experiment" action="create"
                                params="['sample.id': sampleInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'experiment.label', default: 'Experiment')])}</g:link>

                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="study"><g:message code="sample.study.label" default="Study"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'study', 'errors')}">
                        <g:select name="study.id" from="${afterparty.Study.list()}" optionKey="id"
                                  value="${sampleInstance?.study?.id}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="update"
                                                 value="${message(code: 'default.button.update.label', default: 'Update')}"/></span>
            <span class="button"><g:actionSubmit class="delete" action="delete"
                                                 value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                 onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
