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
    <g:if test="${isOwner}">
        <script type="text/javascript">

            //         set up edit-in-place
            $(document).ready(function() {
                setUpEditInPlace(
                        ${studyInstance.id},
                        "<g:createLink controller="update" action="updateField"/>",
                        'Study'
                );


                $(':checkbox').change(function() {
                    updateButton();
                });
                $('#showContigSetsButton').hide();


            });

            function showOnly(class) {
                $(class).show(300);
                $('.compoundSampleRow:not(' + class + ')').hide(300);
            }

            function updateButton() {
                if ($("input:checked").length == 0) {
                    $('#showContigSetsButton').hide();
                    $('#noneSelectedMessage').show();
                }
                if ($("input:checked").length == 1) {
                    $('#showContigSetsButton').show();
                    $('#noneSelectedMessage').hide();
                    $('#showContigSetsButton').val('Compare contig sets');
                }
                if ($("input:checked").length > 1) {
                    $('#showContigSetsButton').show();
                    $('#noneSelectedMessage').hide();
                    $('#showContigSetsButton').val('View contig set details');
                }
            }
        </script>
    </g:if>

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
        <g:if test="${isOwner && !studyInstance.published}">
            <p>
                <g:form action="makePublished" method="get">
                    <g:hiddenField name="id" value="${studyInstance.id}"/>
                    <input type="submit" class="submit long" value="Publish study"/>
                </g:form>
            </p>
        </g:if>

        <g:if test="${isOwner}">
            <g:form controller="study" action="indexForSearching" method="get">
                <g:hiddenField name="id" value="${studyInstance.id}"/>
                <input type="submit" class="submit long" value="Index contigs for searching"/>
            </g:form>
        </g:if>
        <p>
            <g:form controller="study" action="search" method="get">
                <g:hiddenField name="id" value="${studyInstance.id}"/>
                <input type="submit" class="submit long" value="Search contigs"/>
            </g:form>
        </p>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Compound Samples</h2>

        <g:if test="${isOwner}">
            <ul>
                <li>
                    <g:link controller="study" action="createCompoundSample"
                            params="${[id : studyInstance.id]}">Add new</g:link>
                </li>
            </ul>
        </g:if>

    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <g:if test="${studyInstance.compoundSamples}">

            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">
                <thead>
                <tr>
                    <th>Sample name</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${studyInstance.compoundSamples.sort({it.name}) }" var="s">
                    <tr>
                        <td><g:link controller="compoundSample" action="show" id="${s.id}">${s.name}</g:link></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>

        <g:else>
            <h3>Click "ADD NEW" to add a compound sample for this study.</h3>
        </g:else>
    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Contig sets</h2>

        <ul>
            <li>Show only:</li>
            <li>
                <a href="#" onclick="showOnly('.ASSEMBLY');">Assemblies</a>
            </li>
            <li>
                <a href="#" onclick="showOnly('.COMPOUND_SAMPLE');">Compound samples</a>
            </li>

            <li>
                <a href="#" onclick="showOnly('.USER_CREATED');">User created</a>
            </li>
        </ul>

    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <g:form url='[controller: "contigSet", action: "compareContigSetsFromCheckbox"]' id="searchableForm" name="searchableForm" method="get">

            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">
                <thead>
                <tr>
                    <th>Contig Set name</th>
                    <th>Number of Contigs</th>
                </tr>
                </thead>
                <tbody>

                <g:each in="${studyInstance.contigSets.sort({it.name})}" var="contigSet" status="index">
                    <tr class='compoundSampleRow ${contigSet.type}'>
                        <td>
                            <g:checkBox name="check_${contigSet.id}" value="${false}"/> ${contigSet.name}</td>
                        <td>${contigSet.contigs.size()}</td>
                    </tr>
                </g:each>
                </tbody>

            </table>
            <p id="noneSelectedMessage">Select some contig sets to view/compare them</p>
            <input id="showContigSetsButton" type="submit" class="submit long" value="select contig set"/>
        </g:form>

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


%{--only show workflow and structure if we have at least one sample--}%
%{--<g:if test="${studyInstance.compoundSamples}">--}%

%{--<div class="block">--}%
%{--<div class="block_head">--}%
%{--<div class="bheadl"></div>--}%

%{--<div class="bheadr"></div>--}%

%{--<h2>Dataset overviews</h2>--}%
%{--<ul class="tabs">--}%
%{--<li><a href="#tab1">Structure</a></li>--}%
%{--<li><a href="#tab2">Workflow</a></li>--}%
%{--</ul>--}%
%{--</div>        <!-- .block_head ends -->--}%

%{--<div class="block_content tab_content" id="tab1">--}%

%{--<h3>Structure</h3>--}%
%{--<object data=" <g:createLink controller="study" action="overview" params="['id' : studyInstance.id]"/> "--}%
%{--type="image/svg+xml" id="overviewSVG"></object>--}%

%{--</div>        <!-- .block_content ends -->--}%



%{--<div class="block_content tab_content" id="tab2">--}%

%{--<h3>Workflow</h3>--}%

%{--<object data=" <g:createLink controller="backgroundJob" action="overview"--}%
%{--params="['id' : studyInstance.id]"/> "--}%
%{--type="image/svg+xml" id="workflowSVG"></object>--}%

%{--</div>        <!-- .block_content ends -->--}%

%{--<div class="bendl"></div>--}%

%{--<div class="bendr"></div>--}%

%{--</div>--}%
%{--</g:if>--}%

</body>
</html>
