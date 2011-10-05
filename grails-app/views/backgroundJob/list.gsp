<%@ page import="afterparty.BackgroundJob" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'backgroundJob.label', default: 'BackgroundJob')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>

    %{--when the document has loaded, call updateJobStatus to load the job info into the div --}%
    <script type="text/javascript">
        $(document).ready(function() {
            updateJobStatus();
        });
    </script>
</head>

<body>

    <div class="list" id="jobList"></div>

    %{--when the user clicks refresh, call the listAjax method and update the jobList div with the results--}%
    <g:remoteLink elementId="refreshJobList" action="listAjax" update="jobList">Refresh</g:remoteLink>

</body>
</html>
