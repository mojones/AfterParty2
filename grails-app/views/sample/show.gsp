<%@ page import="afterparty.Sample" %>
<html>
<head>
    <meta name="layout" content="main.gsp"/>
    <title>Sample | ${sampleInstance.name}</title>


    %{--set up edit in place. We will grab all elements with class edit_in_place and run the edit in place method on them.
    To make a bit of text editable we need to
    1. add the edit_in_place tag to it
    2. set the name attribute to be the name of the property that the text refers to --}%
    <script type="text/javascript">
        //         set up edit-in-place
        $(document).ready(function() {
            setUpEditInPlace(
                    ${sampleInstance.id},
                    "<g:createLink controller="update" action="updateField"/>",
                    'Sample'
            );
        });
    </script>

</head>

<body>
<div class="row-fluid">
    <div class="span10 offset1">
        <p class="edit_in_place" name="name">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${sampleInstance.name}
        </p>

        <h3>Description</h3>
        <p class="edit_in_place" name="name">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${sampleInstance.description}
        </p>

        <h2>Experiments</h2>
        <g:if test="${isOwner}">
            <ul>
                <g:link controller="sample" action="createExperiment"
                        params="${[id : sampleInstance.id]}">Add new</g:link>
            </ul>
        </g:if>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <g:if test="${sampleInstance.experiments}">
            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">
                <thead>
                <tr>
                    <th>Experiment name</th>
                    <th>Raw reads</th>
                    <td>Run count</td>
                </tr>
                </thead>
                <tbody>
                <g:each in="${sampleInstance.experiments}" var="s">
                    <tr>
                        <td><g:link controller="experiment" action="show" id="${s.id}">${s.name}</g:link></td>
                        <td>${s.rawReadsCount}</td>
                        <td>${s.runs.size()}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>
        <g:else>
            <h3>Click "ADD NEW" to add an experiment for this sample.</h3>
        </g:else>
    </div>        <!-- .block_content ends -->



    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
