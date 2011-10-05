<g:if test="${runningJobList.size > 0}">

    <div class="block runningJob">

        <div class="block_head">
            <div class="bheadl"></div>

            <div class="bheadr"></div>

            <h2>Running jobs</h2>

        </div>        <!-- .block_head ends -->

        <div class="block_content">

            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">

                <thead>
                <tr>
                    <th>Description</th>
                    <th>Progress</th>
                </tr>
                </thead>

                <tbody>
                <g:each in="${runningJobList}" status="i" var="backgroundJobInstance">
                    <tr>
                        <td>${fieldValue(bean: backgroundJobInstance, field: "name")}</td>
                        <td>
                            ${fieldValue(bean: backgroundJobInstance, field: "progress")}
                            <img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                        </td>

                    </tr>
                </g:each>
                </tbody>

            </table>

        </div>        <!-- .block_content ends -->
        <div class="bendl"></div>

        <div class="bendr"></div>
    </div>

</g:if>

<g:if test="${queuedJobList.size > 0}">
    <div class="block">
        <div class="block_head">
            <div class="bheadl"></div>

            <div class="bheadr"></div>

            <h2>Queued jobs</h2>
        </div>        <!-- .block_head ends -->

        <div class="block_content">
            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">

                <thead>
                <tr>
                    <th>Description</th>
                    <th>Progress</th>
                </tr>
                </thead>

                <tbody>
                <g:each in="${queuedJobList}" status="i" var="backgroundJobInstance">
                    <tr>
                        <td>${fieldValue(bean: backgroundJobInstance, field: "name")}</td>
                        <td>
                            ${fieldValue(bean: backgroundJobInstance, field: "progress")}
                            <img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                        </td>

                    </tr>
                </g:each>
                </tbody>

            </table>

        </div>        <!-- .block_content ends -->
        <div class="bendl"></div>

        <div class="bendr"></div>
    </div>

</g:if>



<g:if test="${completedJobList.size > 0}">

    <div class="block">
        <div class="block_head">
            <div class="bheadl"></div>

            <div class="bheadr"></div>

            <h2>Completed jobs</h2>
        </div>        <!-- .block_head ends -->

        <div class="block_content">
            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">

                <thead>
                <tr>
                    <th>Description</th>
                    <th>Progress</th>
                </tr>
                </thead>

                <tbody>
                <g:each in="${completedJobList}" status="i" var="backgroundJobInstance">
                    <tr>
                        <td>${fieldValue(bean: backgroundJobInstance, field: "name")}</td>
                        <td>
                            ${fieldValue(bean: backgroundJobInstance, field: "progress")}
                        </td>

                    </tr>
                </g:each>
                </tbody>

            </table>

        </div>        <!-- .block_content ends -->
        <div class="bendl"></div>

        <div class="bendr"></div>
    </div>

</g:if>

