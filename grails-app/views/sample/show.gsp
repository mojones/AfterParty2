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

        <h2>${sampleInstance.name}<span style="font-size: 10px;">edit</span></h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <p>${sampleInstance.description}</p>
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
            <li><a href="#">Add new</a></li>
            <li><a href="#">Add page</a></li>
        </ul>
    </div>        <!-- .block_head ends -->

    <div class="block_content">

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
    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
