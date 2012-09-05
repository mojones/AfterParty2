<g:if test="${runningJobList.size > 0}">
<div class="row-fluid">
    <div class="span11 offset1">
        <h2>Running jobs</h2>

        <table class="table table-bordered table-hover">

            <thead>
            <tr>
                <th>Description</th>
                <th>Progress</th>
            </tr>
            </thead>

            <tbody>
            <g:each in="${runningJobList}" status="i" var="backgroundJobInstance">
                <tr>
                    <td class="runningJob">${fieldValue(bean: backgroundJobInstance, field: "name")}</td>
                    <td>
                        <i class="icon-time"></i>&nbsp;${fieldValue(bean: backgroundJobInstance, field: "progress")}
                        &nbsp;(${backgroundJobInstance.ETA()})
                        <img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                    </td>

                </tr>
            </g:each>
            </tbody>

        </table>

    </div> 
</div>
        
</g:if>

<g:if test="${queuedJobList.size > 0}">
<div class="row-fluid">
    <div class="span11 offset1">
        <h2>Queued jobs</h2>

        <table  class="table table-bordered table-hover">

            <thead>
            <tr>
                <th>Description</th>
                <th>Progress</th>
            </tr>
            </thead>

            <tbody>
            <g:each in="${queuedJobList}" status="i" var="backgroundJobInstance">
                <tr>
                    <td class="queuedJob">${fieldValue(bean: backgroundJobInstance, field: "name")}</td>
                    <td>
                        <i class="icon-time"></i>&nbsp;${fieldValue(bean: backgroundJobInstance, field: "progress")}
                        <img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                    </td>

                </tr>
            </g:each>
            </tbody>

        </table>

    </div>        <!-- .block_content ends -->
</div>

</g:if>



<g:if test="${completedJobList.size > 0}">
<div class="row-fluid">
    <div class="span11 offset1">
        <h2>Completed jobs</h2>
        <table class="table table-bordered table-hover">

            <thead>
            <tr>
                <th>Description</th>
                <th>Progress</th>
                <th>Destination</th>
            </tr>
            </thead>

            <tbody>
            <g:each in="${completedJobList.sort({-it.startedTime})}" status="i" var="backgroundJobInstance">
                <tr>
                    <td>${fieldValue(bean: backgroundJobInstance, field: "name")}</td>
                    <td>
                        <i class="icon-time"></i>&nbsp;${fieldValue(bean: backgroundJobInstance, field: "progress")}
                    </td>
                    <td><a href="${backgroundJobInstance.destinationUrl}">go to result</a></td>

                </tr>
            </g:each>
            </tbody>

        </table>

    </div>        <!-- .block_content ends -->
</div>

</g:if>

