<%@ page import="afterparty.StatisticsService; afterparty.Study" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="layout" content="main.gsp"/>
<g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
<title>Viewing a set of contigs</title>

<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.jqplot.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.highlighter.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.cursor.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.logAxisRenderer.js')}"></script>
<link rel="stylesheet" href="${resource(dir: 'js', file: 'jquery.jqplot.css')}"/>





<script type="text/javascript">

    function zip2(arrayA, arrayB) {
        var length = Math.min(arrayA.length, arrayB.length);
        var result = [];
        for (var n = 0; n < length; n++) {
            result.push([arrayA[n], arrayB[n]]);
        }
        return result;
    }

    function zip3(arrayA, arrayB, arrayC) {
        var length = Math.min(arrayA.length, arrayB.length, arrayC.length);
        var result = [];
        for (var n = 0; n < length; n++) {
            result.push([arrayA[n], arrayB[n], arrayC[n]]);
        }
        return result;
    }
    function zip4(arrayA, arrayB, arrayC, arrayD) {
        var length = Math.min(arrayA.length, arrayB.length, arrayC.length, arrayD.length);
        var result = [];
        for (var n = 0; n < length; n++) {
            result.push([arrayA[n], arrayB[n], arrayC[n], arrayD[n]]);
        }
        return result;
    }

    // show and hide data series when asked to
    function toggleSeries(index) {
        $('#spinner').show();
        window.series[index] = !window.series[index];
        drawActiveChart();
    }

    function setScatterX(fieldName) {
        window.scatterXField = fieldName;
        $('.scatterx').css({'cursor':'pointer', 'font-weight':'normal'});
        $('#scatterx' + fieldName).css({'cursor':'default', 'font-weight':'bold'});
        drawActiveChart();
    }

    function setScatterY(fieldName) {
        window.scatterYField = fieldName;
        $('.scattery').css({'cursor':'pointer', 'font-weight':'normal'});
        $('#scattery' + fieldName).css({'cursor':'default', 'font-weight':'bold'});
        drawActiveChart();
    }

    //rearrange the data series so that the one with the specified id is last i.e. on the top layer of the scatter plot
    function moveToTop(id) {
        $('#spinner').show();
        var sortedDatasetList = contigSetRawData.contigSetList.sort(function(a, b) {
            return b.id.length - a.id.length
        });
        // empty the dataset list
        window.seriesList = [];
        var currentDataset;
        // first add all the other datasets
        for (var i = 0; i < contigSetRawData.contigSetList.length; i++) {
            currentDataset = contigSetRawData.contigSetList[i];
            if (currentDataset.contigSetId != id) {
                window.seriesList.push(currentDataset);
            }
        }

        // now add the one we want
        for (var j = 0; j < contigSetRawData.contigSetList.length; j++) {
            currentDataset = contigSetRawData.contigSetList[j];
            if (currentDataset.contigSetId == id) {
                window.seriesList.push(currentDataset);
            }
        }
        drawActiveChart();

    }

    drawActiveChart = function() {
        $('#spinner').show();
        if (window.activeChart == 'histogram') {
            $('#histogramDiv').empty();
            setTimeout('drawChart();', 10);
        }

        if (window.activeChart == 'scatterplot') {
            $('#scatterplotDiv').empty();
            setTimeout('drawScatterChart();', 10);
        }
    }

    drawScatterChart = function() {
        $('#scatterplotDiv').empty();
        $('#spinner').show();

        var allLengthValues;
        var fieldName;

        var yAxisRenderer = window.scatterylogOn ? $.jqplot.LogAxisRenderer : $.jqplot.LinearAxisRenderer;
        var xAxisRenderer = window.scatterxlogOn ? $.jqplot.LogAxisRenderer : $.jqplot.LinearAxisRenderer;

        var allValues = window.seriesList.map(function(a) {
            return zip4(a[window.scatterXField], a[window.scatterYField], a.id, a.topBlast);
        });

        var colourList = window.seriesList.map(function(a) {
            return a.colour;
        });
        console.log(allValues);

        var mySeriesOptions = [];
        mySeriesOptions[0] = {markerOptions: {show : window.series[0]}};
        mySeriesOptions[1] = {markerOptions: {show : window.series[1]}};
        mySeriesOptions[2] = {markerOptions: {show : window.series[2]}};
        mySeriesOptions[3] = {markerOptions: {show : window.series[3]}};
        mySeriesOptions[4] = {markerOptions: {show : window.series[4]}};


        scatterPlot = $.jqplot('scatterplotDiv',
                allValues,
                {
                    seriesColors : colourList,
                    title : ' my scatter plot',
                    seriesDefaults:{
                        showMarker: true,
                        showLine: false,
                        markerOptions : {
                            shadow: false,
                            lineWidth : 0,
                            size : 5
                        }
                    },
                    series: mySeriesOptions,
                    axes:{
                        xaxis:{
                            label: window.scatterXField,
                            pad: 0,
                            renderer: xAxisRenderer
                        },
                        yaxis:{
                            label:window.scatterYField,
                            pad : 0,
                            renderer : yAxisRenderer

                        }
                    },
                    highlighter: {
                        show: window.scatterhighlighterOn,
                        tooltipLocation:'ne',
                        sizeAdjust: 7.5,
                        markerRenderer : new $.jqplot.MarkerRenderer({color:'#FFFFFF'}),
                        yvalues: 3,
                        formatString : window.scatterXField + ': %.2f<br/>' + window.scatterYField + ': %.2f<br/>id: %d<br/>blast: %s',
                        useAxesFormatters: false
                    },
                    cursor: {
                        show: !scatterhighlighterOn,
                        tooltipLocation:'sw',
                        followMouse : true,
                        showVerticalLine: true,
                        showHorizontalLine: true,
                        zoom:true
                    }
                }
        );

        $('#scatterplotDiv').bind('jqplotDataClick', function (ev, seriesIndex, pointIndex, data) {
            window.location = '../contig/show/' + data[2];
        });
        $('#spinner').hide();

    }


    drawChart = function() {
        $('#histogramDiv').empty();
        $('#spinner').show();

        var allLengthValues;
        var renderer;
        var fieldName;

        if (scaledOn) {
            fieldName = 'scaled' + window.chartType + 'values';
        } else {
            fieldName = window.chartType + 'values';
        }
        //TODO should this be window.logOn?
        if (logOn) {
            // if we are plotting on a log scale then we will add 0.1 to all the Y values to prevent log(0) error
            allLengthValues = contigSetData.contigSetList.map(function(a) {
                return a[fieldName].map(function(b) {
                    return [
                        b[0], b[1] + 0.1
                    ];
                });
            });
            renderer = $.jqplot.LogAxisRenderer;
        } else {
            console.log(fieldName);
            allLengthValues = contigSetData.contigSetList.map(function(a) {
                return a[fieldName];
            });
            renderer = $.jqplot.LinearAxisRenderer;
        }

        var colourList = contigSetData.contigSetList.map(function(a) {
            return a.colour;
        });
        console.log(colourList);

//        TODO make this less hacky
        var mySeriesOptions = [];
        mySeriesOptions[0] = {showLine : window.series[0]};
        mySeriesOptions[1] = {showLine : window.series[1]};
        mySeriesOptions[2] = {showLine : window.series[2]};
        mySeriesOptions[3] = {showLine : window.series[3]};
        mySeriesOptions[4] = {showLine : window.series[4]};


        histogramPlot = $.jqplot('histogramDiv',
                allLengthValues,
                {
                    seriesColors : colourList,
                    title: window.chartType + ' histogram',
                    seriesDefaults:{
                        showMarker: false,
                        lineWidth: 1
                    },
                    series: mySeriesOptions,
                    axes:{
                        xaxis:{
                            label:window.chartType,
                            pad: 0
                        },
                        yaxis:{
                            label:'Frequency',
                            pad : 0,
                            renderer: renderer

                        }
                    },
                    highlighter: {
                        show: window.highlighterOn,
                        sizeAdjust: 7.5
                    },
                    cursor: {
                        show: !window.highlighterOn,
                        tooltipLocation:'sw',
                        followMouse : true,
                        showVerticalLine: true,
                        showHorizontalLine: true
                    }
                }
        );
        $('#spinner').hide();

    }


    //         set up ajax compare assemblies
    $(document).ready(function() {


        $('#saveSelected').click(function() {
            var xmin = scatterPlot.axes.xaxis.min;
            var xmax = scatterPlot.axes.xaxis.max;
            var ymin = scatterPlot.axes.yaxis.min;
            var ymax = scatterPlot.axes.yaxis.max;

            var ids = [];
            for (var i = 0; i < scatterPlot.data.length; i++) {
                for (var j = 0; j < scatterPlot.data[i].length; j++) {
                    var dataPoint = scatterPlot.data[i][j];
                    if (dataPoint[0] >= xmin && dataPoint[0] <= xmax && dataPoint[1] >= ymin && dataPoint[1] <= ymax) {
                        ids.push(dataPoint[2]);
                    }
                }
            }
            $.post(
                    'createContigSetAJAX',
                    {
                        idList : ids.join(','),
                        studyId : ${contigSets[0].study.id}
                    },
                    function(data) {
                        window.location = '../study/${contigSets[0].study.id}';// + data;
                    }
            );

        });

        // start with all series toggled on
        window.series = [];
        window.series[0] = true;
        window.series[1] = true;
        window.series[2] = true;
        window.series[3] = true;
        window.series[4] = true;
        window.series[5] = true;
        window.series[6] = true;

        // global variables that determine how the chart is drawn
        window.highlighterOn = true;
        window.logOn = false;
        window.scaledOn = false;

        window.scatterhighlighterOn = true;
        window.scaterylogOn = false;
        window.scaterxlogOn = false;


        window.chartType = 'length';

        window.scatterXField = 'length';
        window.scatterYField = 'quality';

        // boring code to handle chart options

        var setUpToggle = function(variableName) {
            $('#turn' + variableName + 'On').click(function() {
                window[variableName + 'On'] = true;
                $('#turn' + variableName + 'Off').css({'cursor':'pointer', 'font-weight':'normal'});
                $('#turn' + variableName + 'On').css({'cursor':'default', 'font-weight':'bold'});
                drawActiveChart();
            });
            $('#turn' + variableName + 'Off').click(function() {
                window[variableName + 'On'] = false;
                $('#turn' + variableName + 'On').css({'cursor':'pointer', 'font-weight':'normal'});
                $('#turn' + variableName + 'Off').css({'cursor':'default', 'font-weight':'bold'});
                drawActiveChart();
            });
        };

        setUpToggle('highlighter');
        setUpToggle('log');
        setUpToggle('scaled');

        setUpToggle('scatterhighlighter');
        setUpToggle('scatterylog');
        setUpToggle('scatterxlog');

        $('#resetZoom').click(function() {
            scatterPlot.resetZoom();
        });


        // handle chart type
        $('#turnlengthOn').click(function() {
            $('#turncoverageOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnqualityOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnlengthOn').css({'cursor':'default', 'font-weight':'bold'});
            window.chartType = 'length';
            setTimeout('drawChart();', 1);
        });
        $('#turncoverageOn').click(function() {
            $('#turnlengthOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnqualityOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turncoverageOn').css({'cursor':'default', 'font-weight':'bold'});
            window.chartType = 'coverage';
            setTimeout('drawChart();', 1);
        });
        $('#turnqualityOn').click(function() {
            $('#turncoverageOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnlengthOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnqualityOn').css({'cursor':'default', 'font-weight':'bold'});
            window.chartType = 'quality';
            setTimeout('drawChart();', 1);
        });

        //show / hide the different chart types
        $('#turnhistogramOn').click(function() {
            $('#turnScatterOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnhistogramOn').css({'cursor':'default', 'font-weight':'bold'});
            $('#scatterplotContainer').hide();
            $('#histogramContainer').show();
            $('#histogramDiv').empty();
            $('#spinner').show();
            window.activeChart = 'histogram';
            setTimeout('drawChart();', 1);

        });
        $('#turnScatterOn').click(function() {
            $('#turnhistogramOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnScatterOn').css({'cursor':'default', 'font-weight':'bold'});
            $('#histogramContainer').hide();
            $('#scatterplotContainer').show();
            $('#scatterplotDiv').empty();
            $('#spinner').show();
            window.activeChart = 'scatterplot';

            setTimeout('drawScatterChart();', 1);

        });

        $('#scatterplotContainer').hide();

        // do the initial get and draw the first chart
        $.get('/contigSet/showContigSetsStatsJSON/?idList=${contigSets*.id.join(',')}', function(data) {
            contigSetData = data;
            drawChart();
        });


        // do the initial get and draw the first chart
        $.get('/contigSet/showContigSetsJSON/?idList=${contigSets*.id.join(',')}', function(data) {
            contigSetRawData = data;
            window.seriesList = [];

            // start off by sorting the data series so that the one with the fewest contigs is on top in the chart - this usually makes it easier to see
            var sortedDatasetList = contigSetRawData.contigSetList.sort(function(a, b) {
                return b.id.length - a.id.length
            });
            for (var i = 0; i < contigSetRawData.contigSetList.length; i++) {
                window.seriesList[i] = sortedDatasetList[i];
            }
        });


    });
</script>

</head>

<body>

<g:if test="${contigSets.size() == 1}">
    <g:set var="contigSetInstance" value="${contigSets[0]}"/>
%{--set up edit in place. We will grab all elements with class edit_in_place and run the edit in place method on them.
To make a bit of text editable we need to
1. add the edit_in_place tag to it
2. set the name attribute to be the name of the property that the text refers to --}%
    <script type="text/javascript">
        //         set up edit-in-place
        $(document).ready(function() {
            setUpEditInPlace(
                    ${contigSetInstance.id},
                    "<g:createLink controller="update" action="updateField"/>",
                    'ContigSet'
            );
        });
    </script>

    <div class="block">

        <div class="block_head">
            <div class="bheadl"></div>

            <div class="bheadr"></div>

            <h2>Contig Set details <span style="font-size: small;">(click to edit)</span></h2>

        </div>        <!-- .block_head ends -->

        <div class="block_content">
            <h3>Name</h3>

            <p class="edit_in_place" name="name">${contigSetInstance.name}</p>

            <h3>Description</h3>

            <p class="edit_in_place" name="description">${contigSetInstance.description}</p>

        </div>        <!-- .block_content ends -->

        <div class="bendl"></div>

        <div class="bendr"></div>
    </div>
</g:if>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Contig sets</h2>

    </div>        <!-- .block_head ends -->

    <div class="block_content">

        <table cellpadding="0" cellspacing="0" width="100%" class="sortable">
            <thead>
            <tr>
                <th>Contig Set name</th>
                <th>Number of Contigs</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${contigSets}" var="contigSet" status="index">
                <tr style="background-color: ${StatisticsService.paleAssemblyColours[index]}">
                    <td>
                        ${contigSet.name} &nbsp;&nbsp;
                        <span style="cursor:pointer;" onclick="toggleSeries(${index});">toggle</span> |
                        <span style="cursor:pointer;" onclick="moveToTop(${contigSet.id});">move to top</span>
                    </td>
                    <td>${contigSet.contigs.size()}</td>
                </tr>
            </g:each>
            </tbody>

        </table>

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Contig Set charts</h2>

    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <p>Chart type : <span id='turnScatterOn' style="cursor: pointer; ">scatter plot</span> | <span style="font-weight: bold;" id='turnhistogramOn'>histogram</span>
        </p>

        %{--TODO possibly replace this with spin.js so that the spinner doesn't freeze while we are drawing the chart--}%
        <h2 id="spinner">Drawing chart, please wait...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
        </h2>

        <div id='histogramContainer'>
            <p>Mouseover : <span id='turnhighlighterOn' style="font-weight: bold;">highlight</span> | <span style="cursor: pointer; " id='turnhighlighterOff'>zoom</span>

                &nbsp;&nbsp;&nbsp;

                Y axis : <span id='turnlogOn' style="cursor: pointer; ">log</span> | <span style="font-weight: bold;" id='turnlogOff'>linear</span>
                &nbsp;&nbsp;&nbsp;

                Scale : <span id='turnscaledOn' style="cursor: pointer; ">per 1000 contigs</span> | <span style="font-weight: bold;" id='turnscaledOff'>raw frequency</span>
            </p>

            <p>
                Chart type : <span id='turnlengthOn' style="font-weight: bold;">length</span> | <span style="cursor: pointer; " id='turnqualityOn'>quality</span> | <span style="cursor: pointer; " id='turncoverageOn'>coverage</span>
            </p>


            <div id="histogramDiv" style="height: 800px; width: 1000px;">

            </div>
        </div>

        <div id='scatterplotContainer'>
            <p>Mouseover :
                <span id='turnscatterhighlighterOn' style="font-weight: bold;">hightlight</span> |
                <span style="cursor: pointer; " id='turnscatterhighlighterOff'>zoom</span>(
                <span style="cursor: pointer;" id="resetZoom">click to reset</span>,
                <span style="cursor: pointer;" id="saveSelected">click to save selected</span>)
            &nbsp;&nbsp;&nbsp;


            &nbsp;&nbsp;&nbsp;

            Y axis : <span id='turnscatterylogOn' style="cursor: pointer; ">log</span> | <span style="font-weight: bold;" id='turnscatterylogOff'>linear</span>
                &nbsp;&nbsp;&nbsp;
                X axis : <span id='turnscatterxlogOn' style="cursor: pointer; ">log</span> | <span style="font-weight: bold;" id='turnscatterxlogOff'>linear</span>
            </p>

            <p>
                X axis :
                <span class="scatterx" id="scatterxlength" style="font-weight: bold;" onclick="setScatterX('length');">length</span> |
                <span class="scatterx" id="scatterxquality" style="cursor: pointer; " onclick="setScatterX('quality');">quality</span> |
                <span class="scatterx" id="scatterxcoverage" style="cursor: pointer; " onclick="setScatterX('coverage');">coverage</span> |
                <span class="scatterx" id="scatterxgc" style="cursor: pointer; " onclick="setScatterX('gc');">gc</span>

            </p>

            <p>
                Y axis :
                <span class="scattery" id="scatterylength" style="font-weight: bold;" onclick="setScatterY('length');">length</span> |
                <span class="scattery" id="scatteryquality" style="cursor: pointer; " onclick="setScatterY('quality');">quality</span> |
                <span class="scattery" id="scatterycoverage" style="cursor: pointer; " onclick="setScatterY('coverage');">coverage</span> |
                <span class="scattery" id="scatterygc" style="cursor: pointer; " onclick="setScatterY('gc');">gc</span>

            </p>


            <div id="scatterplotDiv" style="height: 800px; width: 1000px;">
            </h2>
            </div>
        </div>

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
