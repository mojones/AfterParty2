<%@ page import="afterparty.Sample" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'sample.label', default: 'Sample')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>


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

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Sample details <span style="font-size: small;">(click to edit)</span></h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <h3>Name</h3>

        <p class="edit_in_place" name="name">${sampleInstance.name}</p>

        <h3>Description</h3>

        <p class="edit_in_place" name="description">${sampleInstance.description}</p>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Experiments</h2>

        <ul>
            <g:link controller="experiment" action="create" params="${[sampleId : sampleInstance.id]}">Add new</g:link>
        </ul>
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
