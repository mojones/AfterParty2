    <%@ page import="afterparty.Study" %>
<html>
<head>
    <meta name="layout" content="main.gsp"/>
    <title>Study | ${studyInstance.name}</title>

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
                $("#searchForm").slideUp('slow');
                $('#blastForm').slideUp('slow');
                $('#noneSelectedMessage').slideDown('slow');

            }
            if ($("input:checked").length == 1) {
                $('#noneSelectedMessage').slideUp('slow');
                $('.doSomethingButton').slideDown('slow');

                $('#showContigSetsButton').html('<i class="icon-eye-open"></i>&nbsp;view contigs');
            }
            if ($("input:checked").length > 1) {
                $('#noneSelectedMessage').slideUp('slow');
                $('.doSomethingButton').slideDown('slow');

                $('#showContigSetsButton').html('<i class="icon-eye-open"></i>&nbsp;compare contig sets');
            }
        }

        function showSearchBox() {
            $('#blastForm').slideUp('slow');
            $('#searchForm').slideDown('slow');
            return false;
        }
        function showBLASTBox() {
            $("#searchForm").slideUp('slow');
            $('#blastForm').slideDown('slow');
            return false;
        } 


    </script>

</head>

<body>
<div class="row-fluid">
    <div class="span10 offset1">
        <h2>Study details</h2>
        
        <h3>Name</h3>
        <p class="edit_in_place" name="name">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${studyInstance.name}
        </p>

        <h3>Description</h3>

        <p class="edit_in_place" name="description">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${studyInstance.description}
        </p>
        
        <g:if test="${isOwner && !studyInstance.published}">
            <p>
                <g:form action="makePublished" method="get">
                    <g:hiddenField name="id" value="${studyInstance.id}"/>
                    <input type="submit" class="submit long" value="Publish study"/>
                </g:form>
            </p>
        </g:if>
        <hr/>
    </div>
</div>
<div class="row-fluid">
    <div class="span10 offset1">

        <h2>Compound samples</h2>
            
        <g:if test="${isOwner}">
            <p>
                <g:link class="btn btn-info" controller="study" action="createCompoundSample" params="${[id : studyInstance.id]}">
                    <i class="icon-plus-sign"></i>&nbsp; Add new compound sample
                </g:link>
            </p>
        </g:if>

       <g:if test="${studyInstance.compoundSamples}">

            <table id="compound-sample-table" class="table table-bordered">
                <thead>
                <tr>
                    <th>Compound Sample name</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${studyInstance.compoundSamples.sort({it.name}) }" var="s">
                    <tr>
                        <td><g:link controller="compoundSample" action="show" id="${s.id}"><i class="icon-leaf"></i>&nbsp;${s.name}</g:link></td>
                    </tr>
                </g:each>
                </tbody>
            </table>

            <script type="text/javascript">
                $(document).ready(function() {
                   $('#compound-sample-table').dataTable({
                        "aaSorting": [[ 3, "desc" ]],
                        "asStripeClasses": [],
                        "sPaginationType": "bootstrap"    
                   });
                });
            </script>

        </g:if>

        <g:else>
            <h3>Click "ADD NEW" to add a compound sample for this study.</h3>
        </g:else>
    </div>
</div>
<div class="row-fluid">
    <div class="span10 offset1">

        <h2>Contig sets</h2>
        <!-- <div class="navbar">
            <div class="navbar-inner">
                <a class="brand" href="#">Show only...</a>
                <ul class="nav">
                    <li><a href="#" onclick="showOnly('.compoundSampleRow');"><i class="icon-globe"></i>&nbsp;All contig sets</a></li>
                    <li><a href="#" onclick="showOnly('.STUDY');"><i class="icon-th-list"></i>&nbsp;Study</a></li>
                    <li><a href="#" onclick="showOnly('.ASSEMBLY');"><i class="icon-align-left"></i>&nbsp;Assemblies</a></li>
                    <li><a href="#" onclick="showOnly('.COMPOUND_SAMPLE');"><i class="icon-leaf"></i>&nbsp;Compound samples</a></li>
                    <li><a href="#" onclick="showOnly('.USER');"><i class="icon-filter"></i>&nbsp;User created</a></li>
                </ul>
            </div>
        </div> -->

        <form id="contigSetForm" method="get"  class="form-search">

            <table id="contig-list-table" class="table table-bordered table-hover">
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
                            <g:checkBox name="check_${contigSet.id}" value="${false}" class="checkbox"/> 
                            <i class="icon-tags"></i>&nbsp;${contigSet.name}
                            <g:if test="${contigSet.type.toString() == 'STUDY'}">
                                <span class="label">study</span>
                            </g:if>

                            <g:if test="${contigSet.type.toString() == 'ASSEMBLY'}">
                                <span class="label label-success">assembly</span>
                            </g:if>

                            <g:if test="${contigSet.type.toString() == 'COMPOUND_SAMPLE'}">
                                <span class="label label-important">compound sample</span>
                            </g:if>

                            <g:if test="${contigSet.type.toString() == 'USER'}">
                                <span class="label label-info">user</span>
                            </g:if>
                        </td>
                        <td>${contigSet.numberOfContigs()}</td>
                    </tr>
                </g:each>
                </tbody>

            </table>

            <script type="text/javascript">
                $(document).ready(function() {
                   $('#contig-list-table').dataTable({
                        "aaSorting": [[ 1, "desc" ]],
                        "asStripeClasses": [],
                        "sPaginationType": "bootstrap"    
                   });
                });
            </script>

            <p id="noneSelectedMessage">Select some contig sets to view/compare/search them</p>
            <div class="btn-group">
                <button class="doSomethingButton btn btn-info" id="showContigSetsButton" style="display:none" type="submit" onclick="submitCompare();">
                    <i class="icon-eye-open"></i>&nbsp;view contigs
                </button>
                <button class="doSomethingButton btn btn-info" id="searchContigSetAnnotationButton" style="display:none" onclick="showSearchBox(); return false;" type="submit">
                    <i class="icon-search"></i>&nbsp;search contigs
                </button>
                <button class="doSomethingButton btn btn-info" id="blastContigSetAnnotationButton" style="display:none" onclick="showBLASTBox(); return false;" type="submit">
                    <i class="icon-zoom-in"></i>&nbsp;blast contigs
                </button>
            </div>
            <br/><br/>
            
            <div id="searchForm" style="display:none">

                <div class="input-append">
                    <input name="searchQuery" id="searchQuery" type="text" placeholder="Enter search query..." class="search-query input-xlarge">
                    <button id="submitSearchButton" type="submit" class="btn" onclick="submitSearchForm();">
                        <i class="icon-search"></i>&nbsp;Search
                    </button>    
                </div>
                <span class="help-block">Hint: use <b>&amp;</b> for AND,  <b>|</b> for OR, <b>(</b> and <b>)</b> to group.</span>

                <label>Results to show:</label>
                <select name="numberOfResults">
                    <option value="10">10</option>
                    <option value="100">100</option>
                    <option value="1000">1000</option>
                    <option value="10000">10000</option>
                </select>
                <br/>
                
            </div>

            <div id="blastForm" style="display:none">
                <label>BLAST query sequence:</label> <br/>
                <textarea name="blastQuery" id="blastQuery" rows="10" class="span8" placeholder="Paste DNA sequence here..."></textarea>
                <br/><br/>
                <button id="submitBLASTButton" type="submit" class="btn btn-info" onclick="submitBLASTForm();">
                    <i class="icon-zoom-in"></i>&nbsp;BLAST
                </button>
            </div>

        </form>


    </div>        
</div>        
    



            


</body>
</html>
