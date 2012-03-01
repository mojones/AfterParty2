<%@ page import="afterparty.StatisticsService; afterparty.Study" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="layout" content="standalone.gsp"/>
<g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
<title>Viewing a set of contigs</title>

<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.jqplot.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.highlighter.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.cursor.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.logAxisRenderer.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.pointLabels.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.trendline.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.canvasAxisLabelRenderer.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.canvasTextRenderer.js')}"></script>
<link rel="stylesheet" href="${resource(dir: 'js', file: 'jquery.jqplot.css')}"/>

<style type="text/css">
.jqplot-point-label {
    padding: 1px 3px;
    background-color: #eeeeee;
    font-size: 12px;
}
</style>



<script type="text/javascript">

    function setChartSize(pixels) {
        $('#histogramDiv').height(pixels).width(2 * pixels);
        $('#scatterplotDiv').height(2 * pixels).width(2 * pixels);
        $('#topHistogramDiv').height(pixels/2).width(2 * pixels);
        $('#sideHistogramDiv').height(2 * pixels).width(pixels/2);
        $('#cumulativeDiv').height(2 * pixels).width(2 * pixels);
        drawActiveChart();
    }

    function zipAllWithFilter(arrayA, arrayB, idArray, lengthArray, lengthWithoutNArray, qualityArray, coverageArray, gcArray, topBlastArray, filterFunction) {
        var length = Math.min(arrayA.length, arrayB.length);
        var result = [];
        for (var n = 0; n < length; n++) {

            if (filterFunction(n)) {
                result.push([arrayA[n], arrayB[n], idArray[n], lengthArray[n],lengthWithoutNArray[n], qualityArray[n], coverageArray[n], gcArray[n], topBlastArray[n]]);
            }
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

    function drawTopSideHistograms() {
//now draw histogram above scatter plot


        if (typeof scatterPlot == 'undefined' || typeof window.seriesList == 'undefined') {
            return;
        }

        $('#topHistogramDiv').empty();
        $('#sideHistogramDiv').empty();
        var topHistogramXaxisRenderer;

        var topHistogramData = buildHistogram(window.scatterXField, scatterPlot.axes.xaxis.min, scatterPlot.axes.xaxis.max);
        topHistogramXaxisRenderer = window.scatterxlogOn ? $.jqplot.LogAxisRenderer : $.jqplot.LinearAxisRenderer;

        var colourList = window.seriesList.map(function(a) {
            return a.colour;
        });

        topHistogramPlot = $.jqplot('topHistogramDiv',
                topHistogramData,
                {
                    seriesColors : colourList,
                    seriesDefaults:{
                        showMarker: false,
                        lineWidth: 1
                    },
                    axes:{
                        xaxis:{
                            pad: 0,
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            min : scatterPlot.axes.xaxis.min,
                            max : scatterPlot.axes.xaxis.max,
                            renderer : topHistogramXaxisRenderer,
                            numberTicks : scatterPlot.axes.xaxis.numberTicks

                        },
                        yaxis:{
                            label: 'frequency',
                            pad : 0,
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer
                        }
                    },
                    highlighter: {
                        show: window.highlighterOn,
                        sizeAdjust: 7.5,
                        bringSeriesToFront: true
                    },
                    grid: {
                        background: '#ffffff'
                    }
                }
        )


        //now draw histogram beside scatter plot

        var sideHistogramYaxisRenderer;

        var sideHistogramData = buildHistogram(window.scatterYField).map(function(a) {
            return a.map(function(b) {
                return [b[1], b[0]];
            });
        });
        sideHistogramYaxisRenderer = window.scatterylogOn ? $.jqplot.LogAxisRenderer : $.jqplot.LinearAxisRenderer;


        sideHistogramPlot = $.jqplot('sideHistogramDiv',
                sideHistogramData,
                {   sortData : false,
                    seriesColors : colourList,
                    seriesDefaults:{
                        showMarker: false,
                        lineWidth: 1
                    },
                    axes:{
                        xaxis:{
                            pad: 0,
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            label: 'frequency'
                        },
                        yaxis:{
                            pad : 0,
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            min : scatterPlot.axes.yaxis.min,
                            max : scatterPlot.axes.yaxis.max,
                            renderer : sideHistogramYaxisRenderer,
                            numberTicks : scatterPlot.axes.yaxis.numberTicks
                        }
                    },
                    highlighter: {
                        show: window.highlighterOn,
                        sizeAdjust: 7.5,
                        bringSeriesToFront: true
                    },
                    grid: {
                        background: '#ffffff'
                    }
                }
        )

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

        var realXField = window.scatterXField;
        var realYField = window.scatterYField;

        if (window.scatterXField == 'length' && window.cumulativefilternOn) {
            realXField = 'lengthWithoutN'
        }

        if (window.scatterYField == 'length' && window.cumulativefilternOn) {
            realYField = 'lengthWithoutN'
        }

        var allValues = window.seriesList.map(function(a) {
            return zipAllWithFilter(a[realXField], a[realYField], a.id, a.length, a.lengthWithoutN, a.quality, a.coverage, a.gc, a.topBlast, function(n) {
                return (a.length[n] >= window.minSeqLength && a.coverage[n] >= window.minSeqCoverage);
            });
        });


        var colourList = window.seriesList.map(function(a) {
            return a.colour;
        });
//        console.log(allValues);

        var mySeriesOptions = [];
        for (var i = 0; i < window.seriesList.length; i++) {
            mySeriesOptions.push(
                    {
                        markerOptions: {
                            show : window.series[i]
                        },
                        label : window.seriesList[i].label,
                        trendline: {
                            show: window.scattertrendOn,         // show the trend line
                            color: colourList[i],   // CSS color spec for the trend line.
                            label: '',          // label for the trend line.
                            type: 'linear',     // 'linear', 'exponential' or 'exp'
                            shadow: false,       // show the trend line shadow.
                            lineWidth: 1.5     // width of the trend line.
                        }
                    }
            );
        }
//        console.log(mySeriesOptions);

        scatterPlot = $.jqplot('scatterplotDiv',
                allValues,
                {
                    seriesColors : colourList,
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
                            renderer: xAxisRenderer,
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer

                        },
                        yaxis:{
                            label:window.scatterYField,
                            pad : 0,
                            renderer : yAxisRenderer,
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer

                        }
                    },
                    highlighter: {
                        show: window.scatterhighlighterOn,
                        tooltipLocation:'ne',
                        sizeAdjust: 7.5,
                        markerRenderer : new $.jqplot.MarkerRenderer({color:'#FFFFFF'}),
                        yvalues: 9,
                        formatString : '%.2f,%.2f<br/>id: %d<br/>length: %d (minus Ns : %d)<br/>quality: %d<br/>coverage: %.2f<br/>gc: %.2f',
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

        scatterPlot.postDrawHooks.add(function() {
            drawTopSideHistograms();
        });
        drawTopSideHistograms();

        $('#spinner').hide();
        $('.scatterplotOptions').show();


    }

    buildHistogram = function(fieldName, min, max) {

        if (!max) {
            max = Math.max.apply(Math, contigSetRawData.contigSetList.map(function(set) {
                return Math.max.apply(Math, set[fieldName]);
            }));
        }

        if (!min) {
            min = Math.min.apply(Math, contigSetRawData.contigSetList.map(function(set) {
                return Math.min.apply(Math, set[fieldName]);
            }));
        }

        console.log(min + ' to ' + max);

        var allFieldValues = contigSetRawData.contigSetList.map(function(set) {
            var fieldValues = [];
            var logMax = Math.floor(Math.log(max - min) / Math.LN10);
            var stepSize = Math.max(1, Math.pow(10, logMax - 2));
            var numberOfSteps = Math.floor((max / stepSize)) + 2;

            for (var i = (min / stepSize) - 1; i <= numberOfSteps; i++) {
                var binFloor = i * stepSize;
                var binCeiling = (i + 1) * stepSize;
                var count = set[fieldName].filter(
                        function(element) {
                            return (element >= binFloor && element < binCeiling);
                        }).length;
                if (logOn) {
                    count = count + 0.1;
                }
                if (scaledOn) {
                    count = 1000 * count / set[fieldName].length;
                }
//                console.log(count + ' between ' + binFloor + ' and ' + binCeiling);
                fieldValues.push([binFloor, count]);

            }
            return fieldValues;
        });
        return allFieldValues;
    }


    drawChart = function() {
        $('#histogramDiv').empty();
        $('#spinner').show();

        var allLengthValues;
        var renderer;
        var fieldName = window.chartType;

        if (logOn) {
            renderer = $.jqplot.LogAxisRenderer;
        } else {
            renderer = $.jqplot.LinearAxisRenderer;
        }


        var colourList = contigSetRawData.contigSetList.map(function(a) {
            return a.colour;
        });
//        console.log(colourList);

        var mySeriesOptions = [];
        for (var i = 0; i < contigSetRawData.contigSetList.length; i++) {
            mySeriesOptions.push(
                    {
                        lineOptions: {
                            show : window.series[i]
                        },
                        label : contigSetRawData.contigSetList[i].label
                    }
            );
        }
//        console.log(mySeriesOptions);

        var allFieldValues = buildHistogram(fieldName);

//        console.log('values : ' + allLengthValues);
        histogramPlot = $.jqplot('histogramDiv',
                allFieldValues,
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
                            label: window.chartType,
                            pad: 0
                        },
                        yaxis:{
                            label: scaledOn ? 'Frequency per 1000 contigs' : 'Frequency',
                            pad : 0,
                            renderer: renderer,
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer
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

        var n50values = [];
        var n90values = [];

        var allValues = contigSetRawData.contigSetList.map(function(dataset) {

            var lengthField = window.cumulativefilternOn ? 'lengthWithoutN' : 'length';
            console.log('field will be : ' + lengthField);
            var lengths = dataset[lengthField].filter(
                    function(element) {
                        return (element >= window.minSeqLength);
                    }).sort(function(a, b) {
                        return b - a
                    });

            var lengthsSum = 0;
            for (var i = 0; i < lengths.length; i++) {
                lengthsSum += lengths[i];
            }

            var n50Target = lengthsSum / 2
            var n90Target = (lengthsSum / 100) * 90

            console.log('n50 is ' + n50Target)
            console.log('n90 is ' + n90Target)

            var seenn50 = false;
            var seenn90 = false;

            var returnValue = [];
            var cumulativeTotal = 0;
            for (var i = 0; i < lengths.length; i++) {
                var l = lengths[i];
                cumulativeTotal += l;
                returnValue.push([i, cumulativeTotal, l]);
                if (cumulativeTotal >= n50Target && !seenn50) {
                    n50values.push({
                        contigNumber : i,
                        contigLength : l,
                        totalLength : cumulativeTotal
                    });
                    seenn50 = true;
                }

                if (cumulativeTotal >= n90Target && !seenn90) {
                    n90values.push({
                        contigNumber : i,
                        contigLength : l,
                        totalLength : cumulativeTotal
                    });
                    seenn90 = true;
                }
            }
            return returnValue;
        });

        console.log(n50values);

        var renderer;
        var fieldName;

        var colourList = contigSetRawData.contigSetList.map(function(a) {
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
                [n50values[i].contigNumber, n50values[i].totalLength, 'n50 length : ' + n50values[i].contigLength]
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
                [n90values[i].contigNumber, n90values[i].totalLength, 'n90 length : ' + n90values[i].contigLength]
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
        window.scatterylogOn = true;
        window.scatterxlogOn = false;
        window.scattertrendOn = false;


        window.chartType = 'length';

        window.scatterXField = 'gc';
        window.scatterYField = 'coverage';

        window.minSeqLength = 0;
        window.minSeqCoverage = 0;

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
        setUpToggle('scattertrend');
        setUpToggle('scatterylog');
        setUpToggle('scatterxlog');


        setUpToggle('cumulativefiltern');

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
            $('.histogramField').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnlengthOn').css({'cursor':'default', 'font-weight':'bold'});
            window.chartType = 'length';
            setTimeout('drawChart();', 1);
        });
        $('#turncoverageOn').click(function() {
            $('.histogramField').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turncoverageOn').css({'cursor':'default', 'font-weight':'bold'});
            window.chartType = 'coverage';
            setTimeout('drawChart();', 1);
        });
        $('#turnqualityOn').click(function() {
            $('.histogramField').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turnqualityOn').css({'cursor':'default', 'font-weight':'bold'});
            window.chartType = 'quality';
            setTimeout('drawChart();', 1);
        });
        $('#turngcOn').click(function() {
            $('.histogramField').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turngcOn').css({'cursor':'default', 'font-weight':'bold'});
            window.chartType = 'gc';
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


        %{--contigSetData = {contigSetList : ${contigSetDataJSON}};--}%
        contigSetRawData = {contigSetList : ${contigSetRawDataJSON}};

        window.seriesList = [];
        window.activeChart = 'scatterplot';


        drawChart();

        // start off by sorting the data series so that the one with the fewest contigs is on top in the chart - this usually makes it easier to see
        var sortedDatasetList = contigSetRawData.contigSetList;
        for (var i = 0; i < contigSetRawData.contigSetList.length; i++) {
            window.seriesList[i] = sortedDatasetList[i];
        }

        $('#url').html(document.URL);

    });
</script>

</head>

<body>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Bookmark this page to get back to your analysis</h2>

    </div>        <!-- .block_head ends -->

    <div class="block_content">

        <h2 id="url"></h2>

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


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

        <p>
            Chart size :
            <span onclick="setChartSize(200);" style="cursor: pointer;">tiny</span> |
            <span onclick="setChartSize(300);" style="cursor: pointer;">small</span> |
            <span onclick="setChartSize(400);" style="cursor: pointer;">medium</span> |
            <span onclick="setChartSize(600);" style="cursor: pointer;">large</span> |
            <span onclick="setChartSize(800);" style="cursor: pointer;">huge</span>
        </p>

        <p>
            Minimum sequence length: <input type="text" id="minimumSequenceLength"/> <input type="submit" value="filter" onclick="window.minSeqLength = ($('#minimumSequenceLength').val());
        drawActiveChart();">
            <br/>
            Minimum sequence coverage: <input type="text" id="minimumSequenceCoverage"/> <input type="submit" value="filter" onclick="window.minSeqCoverage = ($('#minimumSequenceCoverage').val());
        drawActiveChart();">
        </p>

        <p>Exclude Ns from length : <span id='turncumulativefilternOff' style="font-weight: bold;">no</span> |
            <span style="cursor: pointer; " id='turncumulativefilternOn'>yes</span>
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
                Chart type :
                <span id='turnlengthOn' class="histogramField" style="font-weight: bold;">length</span> |
                <span style="cursor: pointer; " id='turnqualityOn' class="histogramField">quality</span> |
                <span style="cursor: pointer; " id='turncoverageOn' class="histogramField">coverage</span>|
                <span style="cursor: pointer; " id='turngcOn' class="histogramField">gc</span>
            </p>


            <div id="histogramDiv" style="height: 400px; width: 800px;">

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

            trendlines : <span id='turnscattertrendOff' style="font-weight: bold;">off</span> | <span id='turnscattertrendOn' style="cursor: pointer;">on</span>

                &nbsp;&nbsp;&nbsp;

                Y axis : <span id='turnscatterylogOn' style="font-weight: bold;">log</span> | <span style="cursor: pointer; " id='turnscatterylogOff'>linear</span>
                &nbsp;&nbsp;&nbsp;
                X axis : <span id='turnscatterxlogOn' style="cursor: pointer; ">log</span> | <span style="font-weight: bold;" id='turnscatterxlogOff'>linear</span>
            </p>

            <p class='scatterplotOptions'>
                X axis :
                <span class="scatterx" id="scatterxlength" style="cursor: pointer; " onclick="setScatterX('length');">length</span> |
                <span class="scatterx" id="scatterxquality" style="cursor: pointer; " onclick="setScatterX('quality');">quality</span> |
                <span class="scatterx" id="scatterxcoverage" style="cursor: pointer; " onclick="setScatterX('coverage');">coverage</span> |
                <span class="scatterx" id="scatterxgc" style="font-weight: bold;" onclick="setScatterX('gc');">gc</span>

            </p>

            <p class='scatterplotOptions'>
                Y axis :
                <span class="scattery" id="scatterylength" style="cursor: pointer; " onclick="setScatterY('length');">length</span> |
                <span class="scattery" id="scatteryquality" style="cursor: pointer; " onclick="setScatterY('quality');">quality</span> |
                <span class="scattery" id="scatterycoverage" style="font-weight: bold;" onclick="setScatterY('coverage');">coverage</span> |
                <span class="scattery" id="scatterygc" style="cursor: pointer; " onclick="setScatterY('gc');">gc</span>

            </p>

            <table>
                <tr>
                    <td style="border: none; margin-left: 10px;"><div id="topHistogramDiv" style="height: 200px; width: 800px;"/>
                    </td>
                    <td style="border: none;"></td>
                </tr>
                <tr>
                    <td style="border: none;"><div id="scatterplotDiv" style="height: 800px; width: 800px;"/></td>
                    <td style="border: none;"><div id="sideHistogramDiv" style="height: 800px; width: 200px;"/></td>
                </tr>
            </table>

        </h2>
        </div>
    </div>

</div>        <!-- .block_content ends -->
<div class="bendl"></div>

<div class="bendr"></div>
</div>

</body>
</html>
