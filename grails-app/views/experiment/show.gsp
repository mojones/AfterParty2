<%@ page import="afterparty.Experiment" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'experiment.label', default: 'Experiment')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>


    %{--set up edit in place. We will grab all elements with class edit_in_place and run the edit in place method on them.
    To make a bit of text editable we need to
    1. add the edit_in_place tag to it
    2. set the name attribute to be the name of the property that the text refers to --}%
    <script type="text/javascript">
        //         set up edit-in-place
        $(document).ready(function() {
            setUpEditInPlace(
                    ${experimentInstance.id},
                    "<g:createLink controller="update" action="updateField"/>",
                    'Experiment'
            );
        });
    </script>

</head>

<body>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Experiment details <span style="font-size: small;">(click to edit)</span></h2>

    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <h3>Name</h3>

        <p class="edit_in_place" name="name">${experimentInstance.name}</p>

        <h3>Description</h3>

        <p class="edit_in_place" name="description">${experimentInstance.description}</p>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Runs</h2>
        <g:if test="${isOwner}">
            <ul>
                <g:link controller="experiment" action="createRun"
                        params="${[id : experimentInstance.id]}">Add new</g:link>
            </ul>
        </g:if>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <g:if test="${experimentInstance.runs}">

            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">
                <thead>
                <tr>
                    <th>Run name</th>
                    <th>Raw reads</th>
                    <td>Trimmed reads</td>
                </tr>
                </thead>
                <tbody>
                <g:each in="${experimentInstance.runs}" var="run">
                    <tr>
                        <td><g:link controller="run" action="show" id="${run.id}">${run.name}</g:link></td>
                        <td>${run.getRawReadsCount()}</td>
                        <td>${run.getTrimmedReadsCount()}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>
        <g:else>
            <h3>Click "ADD NEW" to add an run for this experiment.</h3>
        </g:else>
    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<sec:ifLoggedIn>
    <div class="block">

        <div class="block_head">
            <div class="bheadl"></div>

            <div class="bheadr"></div>

            <h2>Upload / Replace adapter sequences</h2>

        </div>        <!-- .block_head ends -->



        <div class="block_content">

            <g:form action="attachAdapterSequences" method="post" enctype="multipart/form-data">

                <p class="fileupload">
                    <label>Select new file:</label><br/>
                    <input type="file" name="myFile"/>

                    <span id="uploadmsg">FASTA format only</span>
                </p>

                <g:hiddenField name="experimentId" value="${experimentInstance?.id}"/>

                <p>
                    <input type="submit" class="submit mid" value="Upload"/>
                </p>
            </g:form>


            <h3><g:link controller="experiment" action="trimAllReadFiles"
                        id="${experimentInstance.id}">Trim all reads</g:link></h3>

        </div>        <!-- .block_content ends -->

        <div class="bendl"></div>

        <div class="bendr"></div>

    </div>

</sec:ifLoggedIn>

</body>
</html>
