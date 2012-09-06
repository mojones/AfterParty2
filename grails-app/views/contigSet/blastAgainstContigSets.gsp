<%@ page import="afterparty.Contig" %>
<html>
<head>
    <meta name="layout" content="main.gsp"/>
    <title>BLAST search results</title>

    %{--raphael library included on this page to show contig annotations, also g plugin and line plugin--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.line-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'coffee-script.js')}"></script>

</head>

<body>

<div class="row-fluid">
    <div class="span10 offset1">
        <h2>Blast hits</h2>

        <script type="text/javascript" src="${resource(dir: 'js', file: 'biodrawing.js')}"></script>
        <script type="text/javascript">

            $(document).ready(function () {

                var data = ${resultsJSON};
                window.mydata = data;
                %{----}%
                var paperWidth = $('#coffeescript_annotation').width() - 20;
                var drawing = new BioDrawing();
                drawing.start(paperWidth, 'coffeescript_annotation');
                drawing.drawSpacer(50);
                drawing.drawTitle('BLAST hits');
                drawing.drawScale(data.query.length);
                console.log(data.query.length);

                for (var i = 0; i < data.hits.length; i++) {
                    var hit = data.hits[i];
                    var hitColour = drawing.getBLASTColour(hit.bitscore);
                    var blastRect = drawing.drawBar(hit.start, hit.stop, 15, hitColour, hit.evalue ,hit.contigName);
                }
                drawing.end();
                $('#spinner').hide();
            });

        </script>


        <h2 id="spinner">Drawing annotation...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;"></h2>
        <div id="coffeescript_annotation"> </div>

        <h2>BLAST results</h2>

                <g:form controller="contigSet" action="createFromContigList" method="post">
                    <g:hiddenField name="q" value="blast search"/>
                    <g:hiddenField name="contigList" value="${results*.contigId.join(',')}"/>
                    <g:hiddenField name="studyId" value="${studyId}"/>

                    <button type="submit" class="btn btn-info"><i class="icon-tags"></i>&nbsp;save as contig set</button>
                </g:form>

        <table id="blast-result-table" class="table table-hover table-bordered">

            <thead>
            <tr>
                <th>Contig</th>
                <th>Assembly</th>
                <th>Compound Sample</th>
                <th>Bitscore</th>
                <th>Evalue</th>
                <th>Start</th>
                <th>Stop</th>

            </tr>
            </thead>

            <tbody>
            <g:each in="${results}" var="b">
                <tr>
                    <td><g:link controller="contig" action="show" id="${b.contigId}">${b.contigName}</g:link></td>
                    <td>${b.assemblyName}</td>
                    <td>${b.compoundSampleName}</td>
                    <td>${b.bitscore}</td>
                    <td>${b.evalue}</td>
                    <td>${b.start}</td>
                    <td>${b.stop}</td>
                </tr>
            </g:each>
            </tbody>

        </table>

                    <script type="text/javascript">
                $(document).ready(function() {
                   $('#blast-result-table').dataTable({
                        "aaSorting": [[ 4, "asc" ]],
                        "asStripeClasses": [],
                        "sPaginationType": "bootstrap"    
                   });
                });
            </script>
    </div>        <!-- .block_content ends -->

</div>

</body>
</html>
