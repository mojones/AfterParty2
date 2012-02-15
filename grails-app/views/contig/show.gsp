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

<p>
    <!-- AddToAny BEGIN -->
    <a class="a2a_dd" href="http://www.addtoany.com/share_save"><img
            src="http://static.addtoany.com/buttons/share_save_256_24.png" width="256" height="24" border="0"
            alt="Share"/></a>
    <script type="text/javascript" src="http://static.addtoany.com/menu/page.js"></script>
    <!-- AddToAny END -->
</p>


<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Annotation</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <script type="text/javascript" src="${resource(dir: 'js', file: 'biodrawing.js')}"></script>
        <script type="text/javascript">

            var drawContig = function(data) {

                var paperWidth = $('#coffeescript_annotation').width() - 20;
                var drawing = new BioDrawing();
                drawing.start(paperWidth, 'coffeescript_annotation');
                drawing.drawSpacer(50);
                drawing.drawTitle('Contig annotation');

                drawing.drawScale(data.length);

                drawing.drawSpacer(50);
                drawing.drawTitle('Quality');
                drawing.drawChart(data.quality, 100);

                drawing.drawSpacer(50);
                drawing.drawTitle('Coverage');
                drawing.drawChart(data.coverage, 100);

                drawing.drawTitle('BLAST hits vs uniprot');
                for (var i = 0; i < data.blastHits.length; i++) {
                    var hit = data.blastHits[i];
                    var hitColour = drawing.getBLASTColour(hit.bitscore);
                    var blastRect = drawing.drawBar(hit.start, hit.stop, 15, hitColour, hit.description, hit.accession);
                    blastRect.hover(
                            function(event) {
                                this.attr({stroke: 'black', 'stroke-width' : '5'});
                                $('#' + hit.accession).css("background-color", "bisque");

                            },
                            function(event) {
                                this.attr({stroke: 'black', 'stroke-width' : '0'});
                                $('#' + hit.accession).css("background-color", "white");
                            }
                    );
                }
                drawing.drawSpacer(50);

                drawing.drawTitle('Reads');

                for (var i = 0; i < data.readColours.length; i++) {
                    var colourMap = data.readColours[i];
                    drawing.drawColouredTitle(colourMap.source, colourMap.colour);
                }


                for (var i = 0; i < data.reads.length; i++) {
                    var read = data.reads[i];
                    var readSource;
                    if (read.sampleSource) {
                        readSource = read.sampleSource;
                    }
                    var readTooltip = readSource.name + ' : ' + read.start + ' - ' + read.stop;

                    var readRect = drawing.drawBar(read.start, read.stop, 10, read.colour, readTooltip, read.name);
                }
                drawing.end();
                $('#spinner').hide();
            }

            $.get('/contig/showJSON/' + ${contigInstance.id}, drawContig);




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

        <h2>${contigInstance.name}</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <h3>Read count : ${contigInstance.reads.size()}</h3>


        <h3>Sequence</h3>
        <textarea rows="10" cols="100">${contigInstance.sequence}</textarea>

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
                <th>Accession</th>
                <th>Bitscore</th>
                <th>Decription</th>
                <th>Start</th>
                <th>Stop</th>

            </tr>
            </thead>

            <tbody>
            <g:each in="${contigInstance.blastHits.sort({-it.bitscore})}" var="b">
                <tr id="${b.accession}">
                    <td>${b.accession}</td>
                    <td>${b.bitscore}</td>
                    <td>${b.description}</td>
                    <td>${b.start}</td>
                    <td>${b.stop}</td>
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
