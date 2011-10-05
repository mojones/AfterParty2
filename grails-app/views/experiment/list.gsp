
<%@ page import="afterparty.Experiment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main.gsp.bak" />
        <g:set var="entityName" value="${message(code: 'experiment.label', default: 'Experiment')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'experiment.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'experiment.name.label', default: 'Name')}" />
                        
                            <g:sortableColumn property="description" title="${message(code: 'experiment.description.label', default: 'Description')}" />
                        
                            <th><g:message code="experiment.adapters.label" default="Adapters" /></th>
                        
                            <th><g:message code="experiment.sample.label" default="Sample" /></th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${experimentInstanceList}" status="i" var="experimentInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${experimentInstance.id}">${fieldValue(bean: experimentInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: experimentInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: experimentInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: experimentInstance, field: "adapters")}</td>
                        
                            <td>${fieldValue(bean: experimentInstance, field: "sample")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${experimentInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
