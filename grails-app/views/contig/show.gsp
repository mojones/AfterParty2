<%@ page import="afterparty.AnnotationType; javassist.bytecode.annotation.Annotation; afterparty.Contig" %>
<html>
<head>
    <meta name="layout" content="main.gsp"/>
    <title>Contig | ${contigInstance.name}</title>

    %{--raphael library included on this page to show contig annotations, also g plugin and line plugin--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.line-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'coffee-script.js')}"></script>

    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.scrollTo-1.4.2-min.js')}"></script>

    <script type="text/javascript" src="${resource(dir: 'js', file: 'bootstrapSwitch.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'portamento.js')}"></script>

</head>


<body>

<div class="row-fluid">
    <div class="span10 offset1">
        <ul class="breadcrumb">
          <li>
            <g:link controller="study" action="show" id="${contigInstance.assembly.compoundSample.study.id}">
                 <g:truncate maxlength="20">${contigInstance.assembly.compoundSample.study.name}</g:truncate>
            </g:link>
            <span class="divider">/</span>
          </li>
          <li>
            <g:link controller="compoundSample" action="show" id="${contigInstance.assembly.compoundSample.id}">
                 <g:truncate maxlength="20">${contigInstance.assembly.compoundSample.name}</g:truncate>
            </g:link>
            <span class="divider">/</span>
          </li>
          <li>
            <g:link controller="assembly" action="show" id="${contigInstance.assembly.id}">
                 <g:truncate maxlength="20">${contigInstance.assembly.name}</g:truncate>
            </g:link>
            <span class="divider">/</span>
          </li>
          <li class="active">${contigInstance.name}</li>
        </ul>
    </div>
</div>

<div class="row-fluid">
    <div class="span8 offset1">
        <h2>${contigInstance.name}</h2>

        <script type="text/javascript" src="${resource(dir: 'js', file: 'biodrawing.js')}"></script>
        <script type="text/javascript">

            var drawContig = function(data) {

                window.contigData = data;
                $('#coffeescript_annotation').empty();
                var paperWidth = $('#coffeescript_annotation').width() - 40;
                var drawing = new BioDrawing();
                drawing.start(paperWidth, 'coffeescript_annotation');
                drawing.drawSpacer(50);
                drawing.drawTitle('Contig annotation');

                drawing.drawScale(window.contigData.length);
                if (Math.min.apply(Math, window.contigData.quality) != Math.max.apply(Math, window.contigData.quality)){
                    drawing.drawSpacer(50);
                    drawing.drawTitle('Quality');
                    drawing.drawChart(window.contigData.quality, 100);
                }

                if (Math.min.apply(Math, window.contigData.coverage) != Math.max.apply(Math, window.contigData.coverage)){
                    drawing.drawSpacer(50);
                    drawing.drawTitle('Coverage');
                    drawing.drawChart(window.contigData.coverage, 100);
                }
                if (window.contigData.blastHits.length > 0) {
                    drawing.drawSpacer(50);
                    drawing.drawTitle('BLAST hits vs uniprot');
                    for (var i = 0; i < window.contigData.blastHits.length; i++) {
                        var hit = window.contigData.blastHits[i];
                        var hitColour = drawing.getBLASTColour(hit.bitscore);
                        var blastRect = drawing.drawBar(hit.start, hit.stop, 15, hitColour, hit.description, hit.accession, hit.id);

                        $('#' + hit.id + '_bar, #' + hit.id + '_text').css('cursor', 'pointer');

                        $('#' + hit.id + '_bar, #' + hit.id + '_text').click(
                                function(a) {
                                    return function(event) {
                                        console.log('clicked ' + a);
                                        $('tr').css('background-color', 'white');
                                        var row = $('#' + a + '_row');
                                        row.css("background-color", "bisque");
                                        $.scrollTo(row, 800, {offset : -300});
                                    }
                                }(hit.id)
                        );
                    }
                    drawing.drawSpacer(50);
                }
                
                // first draw all annotations that are not phobius
                for (type in window.contigData.annotations) {
                    var hits = window.contigData.annotations[type];
                    if (hits.length > 0 && type != 'CONTIG' && type != 'PHOBIUS') {
                        drawing.drawTitle(type + ' annotations');


                        for (var i = 0; i < hits.length; i++) {

                            var hit = hits[i];
                            var hitColour = drawing.getBLASTColour(hit.bitscore);
                            var hitRect = drawing.drawBar(hit.start, hit.stop, 15, 'blue', hit.accession, hit.description + ' (' + type + ')', hit.id);

                            $('#' + hit.id + '_bar, #' + hit.id + '_text').css('cursor', 'pointer');

                            $('#' + hit.id + '_bar, #' + hit.id + '_text').click(
                                    function(a) {
                                        return function(event) {
                                            $('tr').css('background-color', 'white');
                                            var row = $('#' + a + '_row');
                                            row.css("background-color", "bisque");
                                            $.scrollTo(row, 800, {offset : -300});
                                        }
                                    }(hit.id)
                            );
                        }

                        drawing.drawSpacer(50);
                    }
                }
                
                // now draw phobius hits
                var phobius_hits = window.contigData.annotations['PHOBIUS'];
                if (phobius_hits.length > 0 ) {
                    drawing.drawTitle("PHOBIUS annotations");


                    for (var i = 0; i < phobius_hits.length; i++) {

                        var hit = hits[i];
                        if (hit.accession == 'SIGNAL_PEPTIDE' || hit.accession == 'TRANSMEMBRANE' || $('#show_phobius_details_checkbox')[0].checked){
                            var hitColour = drawing.getBLASTColour(hit.bitscore);
                            var hitRect = drawing.drawBar(hit.start, hit.stop, 15, 'blue',  hit.description, '  ' + hit.accession, hit.id);
                        }   
                    }

                    drawing.drawSpacer(50);
                }

    
                if ($('#show_reads_checkbox')[0].checked){
                    drawing.drawTitle('Reads');

                    for (var i = 0; i < window.contigData.readColours.length; i++) {
                        var colourMap = window.contigData.readColours[i];
                        drawing.drawColouredTitle(colourMap.source, colourMap.colour);
                    }


                    for (var i = 0; i < window.contigData.reads.length; i++) {
                        var read = window.contigData.reads[i];
                        var readSource = read.source;

                        var readTooltip = readSource.name + ' : ' + read.start + ' - ' + read.stop;
                        var readRect = drawing.drawBar(read.start, read.stop, 10, read.colour, readTooltip, read.name);

                    }
                }
                drawing.end();
                $('#spinner').hide();
            }
            window.showReads = false;
            $.get('/contig/showJSON/' + ${contigInstance.id}, drawContig);

            

        </script>

        <h2 id="spinner">Drawing annotation...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;"> </h2>
        <div id="coffeescript_annotation" class="in_a_box contig_annotation_box">
        </div>
    </div>
    <div class="span2" id="navi" style="position:fixed; right:10px;">
        <h4>Reads</h4>
       <div class="switch" id="show_reads_switch">
            <input id="show_reads_checkbox" type="checkbox">
    </div> 
        <h4>Full PHOBIUS annotations</h4>
       <div class="switch" id="show_phobius_details_switch" >
            <input id="show_phobius_details_checkbox" type="checkbox">
    </div> 
    </div>
    <script type="text/javascript">
$('#show_phobius_details_switch, #show_reads_switch').on('switch-change', function (e, data) {
drawContig(window.contigData);
});
</script>
</div>
<div class="row-fluid">
    <div class="span8 offset1">

   
        <h3>Read count : ${contigInstance.reads.size()}</h3> 

        <h3>Sequence</h3>
        <textarea rows="5" class="span12">${contigInstance.sequence}</textarea>

        <h2>BLAST hits</h2>

        <table class="table table-bordered table-hover">

            <thead>
            <tr>
                <th>Accession</th>
                <th>Bitscore</th>
                <th>Evalue</th>
                <th>Description</th>
                <th>Start</th>
                <th>Stop</th>
                <th>Source</th>

            </tr>
            </thead>

            <tbody>
            <g:each in="${contigInstance.annotations.findAll({it.type == AnnotationType.BLAST}).sort({-it.bitscore})}" var="b">
                <tr style="cursor: pointer" id="${b.id}_row" onclick="
                    var bar = $('#${b.id}_bar');

                    $.scrollTo(bar, 200, {offset : -300});
                    setTimeout(function() {
                        $('#${b.id}_bar, #${b.id}_text').hide(500, function() {
                            $('#${b.id}_bar, #${b.id}_text').show();
                        });
                    }, 300);

                ">
                    <td><a href="${b.generateUrl()}">${b.accession}</a></td>
                    <td>${b.bitscore}</td>
                    <td style="white-space:nowrap;">${String.format('%10.3G', b.evalue)}</td>
                    <td>${b.description}</td>
                    <td>${b.start}</td>
                    <td>${b.stop}</td>
                    <td>${b.source}</td>
                </tr>
            </g:each>
            </tbody>

        </table>



        <h2>Proten domain annotation</h2>
 
        <table class="table table-bordered table-hover">

            <thead>
            <tr>
                <th>Accession</th>
                <th>Type</th>
                <th>Evalue</th>
                <th>Decription</th>
                <th>Start</th>
                <th>Stop</th>

            </tr>
            </thead>

            <tbody>
            <g:each in="${contigInstance.annotations.findAll({it.type != AnnotationType.BLAST && it.type != AnnotationType.CONTIG && it.type != AnnotationType.PHOBIUS && it.type != AnnotationType.COIL}).sort({it.evalue}).reverse()}" var="b">
                <tr style="cursor: pointer" id="${b.id}_row" onclick="
                    var bar = $('#${b.id}_bar');

                    $.scrollTo(bar, 200, {offset : -300});
                    setTimeout(function() {
                        $('#${b.id}_bar, #${b.id}_text').hide(500, function() {
                            $('#${b.id}_bar, #${b.id}_text').show();
                        });
                    }, 300);

                ">
                    <td><a href="${b.generateUrl()}">${b.accession}</a></td>
                    <td>${b.type}</td>
                    <td>${b.evalue}</td>
                    <td>${b.description}</td>
                    <td>${b.start}</td>
                    <td>${b.stop}</td>
                </tr>
            </g:each>
            </tbody>

        </table>


        <h2>PHOBIUS/COILS annotation</h2>
 
        <table class="table table-bordered table-hover">

            <thead>
            <tr>
                <th>Feature type</th>
                <th>Decription</th>
                <th>Start</th>
                <th>Stop</th>

            </tr>
            </thead>

            <tbody>
            <g:each in="${contigInstance.annotations.findAll({it.type == AnnotationType.PHOBIUS || it.type == AnnotationType.COIL}).sort({it.evalue}).reverse()}" var="b">
                <tr>
                    <td>${b.accession}</td>
                    <td>${b.description}</td>
                    <td>${b.start}</td>
                    <td>${b.stop}</td>
                </tr>
            </g:each>
            </tbody>

        </table>

    </div>       
</div>
</body>
</html>
