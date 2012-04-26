<%@ page import="afterparty.Contig" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'contig.label', default: 'Contig')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>

    %{--raphael library included on this page to show contig annotations, also g plugin and line plugin--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.line-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'coffee-script.js')}"></script>

</head>

<body>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Blast hits</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
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
                    var blastRect = drawing.drawBar(hit.start, hit.stop, 15, hitColour, hit.contigId, hit.contigId);
                }
                drawing.end();
                $('#spinner').hide();
            });






        </script>


        <div id="coffeescript_annotation">
            <h2 id="spinner">Drawing annotation...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
            </h2>
        </div>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>BLAST results</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <h3>Number of hits : ${results.size()}</h3>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>BLAST hits</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">

        <table cellpadding="0" cellspacing="0" width="100%" class="sortable">

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

        <p><input type="submit" class="submit long" value="Save as contig set" onclick="doCreate(${results*.contigId}, ${studyId})"/>
        </p>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
