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


            <g:if test="${isOwner}">
            setUpEditInPlace(
                    ${studyInstance.id},
                    "<g:createLink controller="update" action="updateField"/>",
                    'Study'
            );

            </g:if>

            $(':checkbox').change(function() {
                updateButton();
            });

            $('#showContigSetsButton').hide();

            updateButton()
            $('tr:odd').css('background-color', '#F0F0F0');


        });

        function showOnly(myClass) {
            $('.compoundSampleRow:not(' + myClass + ')').hide();
            $(myClass).show();
            $('tr:odd').css('background-color', '#F0F0F0');
            return false;
        }

        function updateButton() {
            if ($("input:checked").length == 0) {
                $('.doSomethingButton').slideUp('slow');
                $('#noneSelectedMessage').slideDown('slow');
            }
            if ($("input:checked").length == 1) {
                $('#noneSelectedMessage').slideUp('slow');
                $('.doSomethingButton').slideDown('slow');

                $('#showContigSetsButton').val('view contig set');
                $('#searchContigSetAnnotationButton').val('search contig set');
                $('#blastContigSetAnnotationButton').val('BLAST vs contig set');
            }
            if ($("input:checked").length > 1) {
                $('#noneSelectedMessage').slideUp('slow');
                $('.doSomethingButton').slideDown('slow');

                $('#showContigSetsButton').val('compare contig sets');
                $('#searchContigSetAnnotationButton').val('search contig sets');
                $('#blastContigSetAnnotationButton').val('BLAST vs contig sets');
            }
        }


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
        <g:if test="${isOwner && !studyInstance.published}">
            <p>
                <g:form action="makePublished" method="get">
                    <g:hiddenField name="id" value="${studyInstance.id}"/>
                    <input type="submit" class="submit long" value="Publish study"/>
                </g:form>
            </p>
        </g:if>

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
                <span class="clickable" onclick="showOnly('.compoundSampleRow');">All contig sets</span>
            </li>
            <li>
                <span class="clickable" onclick="showOnly('.STUDY');">Study</span>
            </li>
            <li>
                <span class="clickable" onclick="showOnly('.ASSEMBLY');">Assemblies</span>
            </li>
            <li>
                <span class="clickable" onclick="showOnly('.COMPOUND_SAMPLE');">Compound samples</span>
            </li>
            <li>
                <span class="clickable" onclick="showOnly('.USER');">User created</span>
            </li>
        </ul>

    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <form id="contigSetForm" method="get">

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
                            <g:checkBox name="check_${contigSet.id}" value="${false}" class="checkbox"/> ${contigSet.name}</td>
                        <td>${contigSet.numberOfContigs()}</td>
                    </tr>
                </g:each>
                </tbody>

            </table>

            <p id="noneSelectedMessage">Select some contig sets to view/compare/search them</p>

            <input class="doSomethingButton submit long" id="showContigSetsButton" style="display:none" type="submit" value="select contig set" onclick="submitCompare();"/>

            <input class="doSomethingButton submit long" id="searchContigSetAnnotationButton" style="display:none" onclick="showSearchBox();
            return false;" type="submit" class="submit long" value="search contig sets">
            <input class="doSomethingButton submit long" id="blastContigSetAnnotationButton" style="display:none" onclick="showBLASTBox();
            return false;" type="submit" class="submit long" value="search contig sets">

            <br/>

            <p id="blastForm" style="display:none">
                <label>BLAST query sequence:</label> <br/><br/>
                <textarea name="blastQuery" id="blastQuery" rows="40" cols="80"></textarea>
                <br/><br/>
                <input id="submitBLASTButton" type="submit" class="submit long" value="submit" onclick="submitBLASTForm();">
            </p>

            <p id="searchForm" style="display:none">
                <label>Search query:</label> <br/><br/>
                <input name="searchQuery" id="searchQuery" type="text" class="text small">
                <label>Results to show:</label>
                <select name="numberOfResults">
                    <option value="10">10</option>
                    <option value="100">100</option>
                    <option value="1000">1000</option>
                    <option value="10000">10000</option>
                </select>
                <br/><br/>
                Hint: use <b>&amp;</b> for AND,  <b>|</b> for OR, <b>(</b> and <b>)</b> to group.
                <br/><br/>

                <input id="submitSearchButton" type="submit" class="submit long" value="submit" onclick="submitSearchForm();">
            </p>
        </form>
    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
