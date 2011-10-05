

<%@ page import="afterparty.Experiment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main.gsp.bak" />
        <g:set var="entityName" value="${message(code: 'experiment.label', default: 'Experiment')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${experimentInstance}">
            <div class="errors">
                <g:renderErrors bean="${experimentInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${experimentInstance?.id}" />
                <g:hiddenField name="version" value="${experimentInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="experiment.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: experimentInstance, field: 'name', 'errors')}">
                                    <g:textArea name="name" cols="40" rows="5" value="${experimentInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><g:message code="experiment.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: experimentInstance, field: 'description', 'errors')}">
                                    <g:textArea name="description" cols="40" rows="5" value="${experimentInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="adapters"><g:message code="experiment.adapters.label" default="Adapters" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: experimentInstance, field: 'adapters', 'errors')}">
                                    <g:select name="adapters.id" from="${afterparty.AdaptersFile.list()}" optionKey="id" value="${experimentInstance?.adapters?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="runs"><g:message code="experiment.runs.label" default="Runs" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: experimentInstance, field: 'runs', 'errors')}">
                                    
<ul>
<g:each in="${experimentInstance?.runs?}" var="r">
    <li><g:link controller="run" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="run" action="create" params="['experiment.id': experimentInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'run.label', default: 'Run')])}</g:link>

                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="sample"><g:message code="experiment.sample.label" default="Sample" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: experimentInstance, field: 'sample', 'errors')}">
                                    <g:select name="sample.id" from="${afterparty.Sample.list()}" optionKey="id" value="${experimentInstance?.sample?.id}"  />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
