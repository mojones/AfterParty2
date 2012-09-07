<%@ page import="afterparty.StatisticsService; afterparty.Study" %>
<html>
<head>
    <meta name="layout" content="main.gsp"/>
    <title>Compound sample | ${compoundSample.name}</title>

    %{--we need raphael to draw comparison graphs--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.raphael-min.js')}"></script>
    %{--<script type="text/javascript" src="${resource(dir: 'js', file: 'g.line-min.js')}"></script>--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.line.custom.js')}"></script>



%{--set up edit in place. We will grab all elements with class edit_in_place and run the edit in place method on them.
To make a bit of text editable we need to
1. add the edit_in_place tag to it
2. set the name attribute to be the name of the property that the text refers to --}%
    <g:if test="${isOwner}">
        <script type="text/javascript">

            //         set up edit-in-place
            $(document).ready(function() {

                setUpEditInPlace(
                        ${compoundSample.id},
                        "<g:createLink controller="update" action="updateField"/>",
                        'CompoundSample'
                );

            });
        </script>
    </g:if>

</head>

<body>
<div class="row-fluid">
    <div class="span10 offset1">
        <h2>Compound sample details</h2>
        <h3>Name</h3>

        <p class="edit_in_place" name="name">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${compoundSample.name}
        </p>

        <g:if test="${compoundSample.defaultContigSet}">
            <g:render template="/contigSet/searchForm" model="['contigSetId' : compoundSample.defaultContigSet.id, 'readSources' : readSources]"/>
        </g:if>

        <h2>Samples</h2>
        <g:if test="${isOwner}">
            <p>
                <g:link class="btn btn-info" controller="compoundSample" action="createSample" params="${[id : compoundSample.id]}">
                    <i class="icon-plus-sign"></i>&nbsp; Add new sample
                </g:link>
            </p>
        </g:if>

        <g:if test="${compoundSample.samples}">

            <table class="table table-bordered table-hover">
                <thead>
                <tr>
                    <th>Sample name</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${compoundSample.samples}" var="s">
                    <tr>
                        <td><g:link controller="sample" action="show" id="${s.id}"><i class="icon-tag"></i>&nbsp;${s.name}</g:link></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>

        <g:else>
            <h3>Click "ADD NEW" to add a sample for this compound sample.</h3>
        </g:else>
    

        <h2>Assemblies</h2>
        <g:if test="${isOwner}">
            <p>
                <g:link class="btn btn-info" controller="compoundSample" action="createAssembly" params="${[id : compoundSample.id]}">
                    <i class="icon-plus-sign"></i>&nbsp; Add new assembly
                </g:link>
            </p>
        </g:if>
    
        <g:if test="${compoundSample.assemblies}">

            <table class="table table-bordered table-hover">
                <thead>
                <tr>
                    <th>Assembly name</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${compoundSample.assemblies.sort()}" var="assembly" status="index">
                    <tr>
                        <td><g:link controller="assembly" action="show" id="${assembly.id}"><i class="icon-align-left"></i>&nbsp;${assembly.name}</g:link></td>
                    </tr>
                </g:each>
                </tbody>

            </table>

        </g:if>
        <g:else>
            <h3>Click "ADD NEW" to add an assembly for this species.</h3>
        </g:else>

       

        <g:if test="${compoundSample.defaultContigSet}">
            <h2>Browse contigs for this compound sample</h2>
            <g:render template="/contigSet/contigTable" model="[contigSetId : compoundSample.defaultContigSet.id]"/>
        </g:if>



    </div>        
</div>

</body>
</html>
