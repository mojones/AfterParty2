<%@ page import="afterparty.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>

    %{--set up edit in place. We will grab all elements with class edit_in_place and run the edit in place method on them.
    To make a bit of text editable we need to
    1. add the edit_in_place tag to it
    2. set the name attribute to be the name of the property that the text refers to --}%
    <script type="text/javascript">

        //         set up edit-in-place
        $(document).ready(function() {
            setUpEditInPlace(
                    ${studyInstance.id},
                    "<g:createLink controller="update" action="updateField"/>",
                    'Study'
            );
        });
    </script>

</head>

<body>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Study details</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <h3>Name</h3>

        <p class="edit_in_place" name="name">${studyInstance.name}</p>

        <h3>Description</h3>

        <p class="edit_in_place" name="description">${studyInstance.description}</p>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Samples</h2>

        <ul>
            <li><a href="#">Add new</a></li>
            <li><a href="#">Add page</a></li>
        </ul>
    </div>        <!-- .block_head ends -->

    <div class="block_content">

        <table cellpadding="0" cellspacing="0" width="100%" class="sortable">
            <thead>
            <tr>
                <th>Sample name</th>
                <th>Raw reads</th>
                <td>Experiment count</td>
            </tr>
            </thead>
            <tbody>
            <g:each in="${studyInstance.samples}" var="s">
                <tr>
                    <td><g:link controller="sample" action="show" id="${s.id}">${s.name}</g:link></td>
                    <td>${s.rawReadsCount}</td>
                    <td>${s.experiments.size()}</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


<div class="block">
    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Dataset overviews</h2>
        <ul class="tabs">
            <li><a href="#tab1">Structure</a></li>
            <li><a href="#tab2">Workflow</a></li>
        </ul>
    </div>        <!-- .block_head ends -->

    <div class="block_content tab_content" id="tab1">

        <h3>Structure</h3>
        <object data=" <g:createLink controller="study" action="overview" params="['id' : studyInstance.id]"/> "
                type="image/svg+xml" id="overviewSVG"></object>

    </div>        <!-- .block_content ends -->



    <div class="block_content tab_content" id="tab2">

        <h3>Workflow</h3>

        <object data=" <g:createLink controller="backgroundJob" action="overview" params="['id' : studyInstance.id]"/> "
                type="image/svg+xml" id="workflowSVG"></object>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>

</div>

</body>
</html>
