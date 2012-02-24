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
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.pointLabels.js')}"></script>
<link rel="stylesheet" href="${resource(dir: 'js', file: 'jquery.jqplot.css')}"/>

<style type="text/css">
.jqplot-point-label {
    padding: 1px 3px;
    background-color: #eeeeee;
    font-size: 12px;
}
</style>



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
        $('.scatterplotOptions').hide();
        drawActiveChart();
    }

    function setScatterY(fieldName) {
        window.scatterYField = fieldName;
        $('.scattery').css({'cursor':'pointer', 'font-weight':'normal'});
        $('#scattery' + fieldName).css({'cursor':'default', 'font-weight':'bold'});
        $('.scatterplotOptions').hide();
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

        if (window.activeChart == 'cumulative') {
            $('#cumulativeDiv').empty();
            setTimeout('drawCumulativeChart();', 10);
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
        for (var i = 0; i < window.seriesList.length; i++) {
            mySeriesOptions.push(
                    {
                        markerOptions: {
                            show : window.series[i]
                        },
                        label : window.seriesList[i].label
                    }
            );
        }
        console.log(mySeriesOptions);

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
                        useAxesFormatters: false,
                        bringSeriesToFront: true

                    },
                    cursor: {
                        show: !scatterhighlighterOn,
                        tooltipLocation:'sw',
                        followMouse : true,
                        showVerticalLine: true,
                        showHorizontalLine: true,
                        zoom:true
                    },
                    legend:{
                        show: true
                    },
                    grid: {
                        background: '#ffffff'
                    }
                }
        );

        $('#scatterplotDiv').bind('jqplotDataClick', function (ev, seriesIndex, pointIndex, data) {
            window.location = '../contig/show/' + data[2];
        });
        $('#spinner').hide();
        $('.scatterplotOptions').show();


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

        var mySeriesOptions = [];
        for (var i = 0; i < contigSetData.contigSetList.length; i++) {
            mySeriesOptions.push(
                    {
                        lineOptions: {
                            show : window.series[i]
                        },
                        label : contigSetData.contigSetList[i].id
                    }
            );
        }
        console.log(mySeriesOptions);

        console.log('values : ' + allLengthValues);
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
                        sizeAdjust: 7.5,
                        bringSeriesToFront: true
                    },
                    cursor: {
                        show: !window.highlighterOn,
                        tooltipLocation:'sw',
                        followMouse : true,
                        showVerticalLine: true,
                        showHorizontalLine: true,
                        zoom: true,
                        constrainZoomTo : 'x'
                    } ,
                    legend:{
                        show: true
                    },
                    grid: {
                        background: '#ffffff'
                    }
                }
        );
        $('#spinner').hide();

    }
    drawCumulativeChart = function() {
        $('#cumulativeDiv').empty();
        $('#spinner').show();

        var allValues = contigSetRawData.contigSetList.map(function(dataset) {
            var lengths = dataset.length.sort(function(a, b) {
                return b - a
            });
            var returnValue = [];
            var cumulativeTotal = 0;
            for (var i = 0; i < lengths.length; i++) {
                var l = lengths[i];
                cumulativeTotal += l;
                returnValue.push([i, cumulativeTotal, l]);
            }
            return returnValue;
        });


        var renderer;
        var fieldName;

        var colourList = contigSetData.contigSetList.map(function(a) {
            return a.colour;
        });

        var mySeriesOptions = [];
        for (var i = 0; i < window.seriesList.length; i++) {


            mySeriesOptions.push(
                    {

                        showLine : window.series[i],
                        showLabel : window.series[i],
                        label : window.seriesList[i].label

                    }
            );


        }

        // go through the list of contig sets again and add a new series to hold n50values
        for (var i = 0; i < window.seriesList.length; i++) {

            allValues.push([
                [contigSetRawData.contigSetList[i].n50Contig, contigSetRawData.contigSetList[i].n50Total, 'n50']
            ]);

            mySeriesOptions.push(
                    {
                        showMarker: window.series[i],
                        showLabel : false,
                        showLine : false,
                        pointLabels: {
                            show:window.series[i],
                            ypadding : 5,
                            xpadding : 5,
                            location : 'nw'
                        }
                    }
            );
        }

        // go through the list of contig sets again and add a new series to hold n90values
        for (var i = 0; i < window.seriesList.length; i++) {

            allValues.push([
                [contigSetRawData.contigSetList[i].n90Contig, contigSetRawData.contigSetList[i].n90Total, 'n90']
//                [contigSetRawData.contigSetList[i].smallContig, contigSetRawData.contigSetList[i].smallTotal, 'small contigs']
            ]);

            mySeriesOptions.push(
                    {
                        showMarker: window.series[i],
                        showLabel : false,
                        showLine : false,
                        pointLabels: {
                            show:window.series[i],
                            ypadding : 5,
                            xpadding : 5,
                            location : 'ne'
                        }
                    }
            );
        }

        // go through the list of contig sets again and add a new series to hold small contig values
        for (var i = 0; i < window.seriesList.length; i++) {

            allValues.push([
                [contigSetRawData.contigSetList[i].smallContig, contigSetRawData.contigSetList[i].smallTotal, 'contigs < 2000bp']
            ]);

            mySeriesOptions.push(
                    {
                        showMarker: window.series[i],
                        showLabel : false,
                        showLine : false,
                        pointLabels: {
                            show:window.series[i],
                            ypadding : 5,
                            xpadding : 5,
                            location : 'se'
                        }
                    }
            );
        }


        console.log("options : " + mySeriesOptions);

        cumulativePlot = $.jqplot('cumulativeDiv',
                allValues,
                {
                    seriesColors : colourList,
                    title: 'cumulative contig lengths',
                    seriesDefaults:{
                        showMarker: false,
                        lineWidth: 1
                    },
                    series: mySeriesOptions,
                    axes:{
                        xaxis:{
                            label: 'Contigs ranked by length',
                            pad: 0
                        },
                        yaxis:{
                            label:'Cumulative contig length',
                            pad : 0,
                            renderer: renderer

                        }
                    },
                    highlighter: {
                        show: window.highlighterOn,
                        sizeAdjust: 7.5,
                        bringSeriesToFront: true,
                        yvalues: 2,
                        formatString : 'rank : %d<br/>cumulative length : %d<br/>contig length: %d<br/>',
                        useAxesFormatters: false
                    },
                    cursor: {
                        show: !window.highlighterOn,
                        tooltipLocation:'sw',
                        followMouse : true,
                        showVerticalLine: true,
                        showHorizontalLine: true,
                        zoom: true,
                        constrainZoomTo : 'x'
                    },
                    legend:{
                        show: true,
                        location: 'se'
                    },
                    grid: {
                        background: '#ffffff'
                    }
                }
        );
        $('#spinner').hide();

    }


    //         set up ajax compare assemblies
    $(document).ready(function() {

        // start with all series toggled on
        window.series = [];
        window.series[0] = true;
        window.series[1] = true;
        window.series[2] = true;
        window.series[3] = true;
        window.series[4] = true;
        window.series[5] = true;
        window.series[6] = true;
        window.series[7] = true;
        window.series[8] = true;
        window.series[9] = true;

        // global variables that determine how the chart is drawn
        window.highlighterOn = true;
        window.logOn = false;
        window.scaledOn = false;

        window.scatterhighlighterOn = true;
        window.scaterylogOn = false;
        window.scaterxlogOn = false;


        window.chartType = 'length';

        window.scatterXField = 'length';
        window.scatterYField = 'coverage';

        // boring code to handle chart options

        var setUpToggle = function(variableName) {
            $('#turn' + variableName + 'On').click(function() {
                window[variableName + 'On'] = true;
                $('#turn' + variableName + 'Off').css({'cursor':'pointer', 'font-weight':'normal'});
                $('#turn' + variableName + 'On').css({'cursor':'default', 'font-weight':'bold'});
                $('.scatterplotOptions').hide();
                drawActiveChart();
            });
            $('#turn' + variableName + 'Off').click(function() {
                window[variableName + 'On'] = false;
                $('#turn' + variableName + 'On').css({'cursor':'pointer', 'font-weight':'normal'});
                $('#turn' + variableName + 'Off').css({'cursor':'default', 'font-weight':'bold'});
                $('.scatterplotOptions').hide();
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
        $('#resetHistogramZoom').click(function() {
            histogramPlot.resetZoom();
        });
        $('#resetCumulativeZoom').click(function() {
            cumulativePlot.resetZoom();
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
            $('#turncumulativeOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnhistogramOn').css({'cursor':'default', 'font-weight':'bold'});
            $('#cumulativeContainer').hide();
            $('#scatterplotContainer').hide();
            $('#histogramContainer').show();
            $('#histogramDiv').empty();
            $('#spinner').show();
            window.activeChart = 'histogram';
            setTimeout('drawChart();', 1);

        });
        $('#turnScatterOn').click(function() {
            $('#turnhistogramOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turncumulativeOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnScatterOn').css({'cursor':'default', 'font-weight':'bold'});
            $('#histogramContainer').hide();
            $('#cumulativeContainer').hide();
            $('#scatterplotContainer').show();
            $('#scatterplotDiv').empty();
            $('#spinner').show();
            window.activeChart = 'scatterplot';

            setTimeout('drawScatterChart();', 1);

        });
        $('#turncumulativeOn').click(function() {
            $('#turnhistogramOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnScatterOn').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turncumulativeOn').css({'cursor':'default', 'font-weight':'bold'});
            $('#histogramContainer').hide();
            $('#scatterplotContainer').hide();
            $('#cumulativeContainer').show();

            window.activeChart = 'cumulative';

            setTimeout('drawCumulativeChart();', 1);

        });

        $('#scatterplotContainer').hide();
        $('#cumulativeContainer').hide();


        contigSetData = {contigSetList : ${contigSetDataJSON}};
        window.activeChart = 'histogram';
        drawChart();


        contigSetRawData = {contigSetList : ${contigSetRawDataJSON}};
        window.seriesList = [];

        // start off by sorting the data series so that the one with the fewest contigs is on top in the chart - this usually makes it easier to see
        var sortedDatasetList = contigSetRawData.contigSetList;
        for (var i = 0; i < contigSetRawData.contigSetList.length; i++) {
            window.seriesList[i] = sortedDatasetList[i];
        }


    });
</script>

</head>

<body>

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
                <th>Colour</th>
                <th>Contig Set name</th>
                <th>Number of Contigs</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${contigSets}" var="contigSet" status="index">
                <tr>
                    <td style="background-color: ${StatisticsService.boldAssemblyColours[index]}">.</td>
                    <td>
                        ${contigSet.label} &nbsp;&nbsp;
                        <span style="cursor:pointer;" onclick="toggleSeries(${index});">toggle</span> |
                        <span style="cursor:pointer;" onclick="moveToTop(${contigSet.id});">move to top</span>
                    </td>
                    <td>${contigSet.size}</td>
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

        <p>Chart type : <span id='turnScatterOn' style="cursor: pointer; ">scatter plot</span> | <span style="font-weight: bold;" id='turnhistogramOn'>histogram</span> | <span style="cursor: pointer; " id='turncumulativeOn'>cumulative length</span>
        </p>

        %{--TODO possibly replace this with spin.js so that the spinner doesn't freeze while we are drawing the chart--}%
        <h2 id="spinner">Drawing chart, please wait...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
        </h2>

        <div id='histogramContainer'>
            <p>Mouseover : <span id='turnhighlighterOn' style="font-weight: bold;">highlight</span> |
                <span style="cursor: pointer; " id='turnhighlighterOff'>zoom</span> (
                <span style="cursor: pointer;" id="resetHistogramZoom">click to reset</span>)

            </p>

            <p>

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

        <div id='cumulativeContainer'>
            <p>Mouseover : <span id='turncumulativehighlighterOn' style="font-weight: bold;">highlight</span> |
                <span style="cursor: pointer; " id='turncumulativehighlighterOff'>zoom</span> (
                <span style="cursor: pointer;" id="resetCumulativeZoom">click to reset</span>)

            </p>

            <div id="cumulativeDiv" style="height: 800px; width: 1000px;">

            </div>
        </div>

        <div id='scatterplotContainer'>
            <p class='scatterplotOptions'>Mouseover :
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

            <p class='scatterplotOptions'>
                X axis :
                <span class="scatterx" id="scatterxlength" style="font-weight: bold;" onclick="setScatterX('length');">length</span> |
                <span class="scatterx" id="scatterxquality" style="cursor: pointer; " onclick="setScatterX('quality');">quality</span> |
                <span class="scatterx" id="scatterxcoverage" style="cursor: pointer; " onclick="setScatterX('coverage');">coverage</span> |
                <span class="scatterx" id="scatterxgc" style="cursor: pointer; " onclick="setScatterX('gc');">gc</span>

            </p>

            <p class='scatterplotOptions'>
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
