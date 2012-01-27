<%@ page import="afterparty.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
    <title>Viewing a compound sample</title>

    %{--we need raphael to draw comparison graphs--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.line-min.js')}"></script>



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

    <script type="text/javascript">

        //         set up ajax compare assemblies
        $(document).ready(function() {
            $('#spinner').hide();

            $('#spinner').show();
            $.get('/compoundSample/showAssembliesJSON/' + ${compoundSample.id}, function(data) {

                // create at top left corner of #element
                var raphaelWidth = $('#comparison').width() - 40;
                var chartWidth = raphaelWidth - 400;
                var r = Raphael('comparison', raphaelWidth, 1000);
                var lengthYvalues = data.assemblyList.map(function(a) {
                    return a.lengthYvalues
                });
                var colours = data.assemblyList.map(function(a) {
                    return a.colour
                });
                var linechart = r.g.linechart(100, 100, chartWidth, 600, data.assemblyList[0].lengthXvalues, lengthYvalues, {colors:colours, axis:"0 0 1 1"});
//                    var linechart = r.g.linechart(100, 100, chartWidth, 600, , [[3,2,6,5,1,4,5,8,5], [6,2,5,4,1,8,7,5,4]], {"symbol":"", axis:"0 0 1 1"});
                $('#spinner').hide();
            });


        });
    </script>
</head>

<body>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Compound sample details</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <h3>Name</h3>

        <p class="edit_in_place" name="name">${compoundSample.name}</p>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Samples</h2>

        <g:if test="${isOwner}">
            <ul>
                <li>
                    <g:link controller="compoundSample" action="createSample"
                            params="${[id : compoundSample.id]}">Add new</g:link>
                </li>
            </ul>
        </g:if>

    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <g:if test="${compoundSample.samples}">

            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">
                <thead>
                <tr>
                    <th>Sample name</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${compoundSample.samples}" var="s">
                    <tr>
                        <td><g:link controller="sample" action="show" id="${s.id}">${s.name}</g:link></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>

        <g:else>
            <h3>Click "ADD NEW" to add a sample for this compound sample.</h3>
        </g:else>
    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Assemblies</h2>
        <g:if test="${isOwner}">

            <ul>
                <li>
                    <g:link controller="compoundSample" action="createAssembly"
                            params="${[id : compoundSample.id]}">Add new</g:link>
                </li>
            </ul>
        </g:if>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <g:if test="${compoundSample.assemblies}">

            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">
                <thead>
                <tr>
                    <th>Assembly name</th>
                    <th>Number of Contigs</th>
                    <td>Span</td>
                </tr>
                </thead>
                <tbody>
                <g:each in="${compoundSample.assemblies}" var="assembly">
                    <tr>
                        <td><g:link controller="assembly" action="show" id="${assembly.id}">${assembly.name}</g:link></td>
                        <td>${assembly.contigs.size()}</td>
                        <td>${assembly.baseCount}</td>
                    </tr>
                </g:each>
                </tbody>

            </table>

            <div id="comparison" style="height: 1000px;">
                <h2 id="spinner">Drawing graphs...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                </h2>
            </div>

            <p>
                <input id='compareAssembliesButton' type="submit" class="submit small" value="Compare"/>
            </p>

        </g:if>
        <g:else>
            <h3>Click "ADD NEW" to add an assembly for this species.</h3>
        </g:else>
    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
