



// set the size of the divs that will hold charts, then redraw the active chart
function setChartSize(pixels) {
    $('#histogramDiv').height(pixels).width(2 * pixels);
    $('#scatterplotDiv').height(2 * pixels).width(2 * pixels);
    $('#topHistogramDiv').height(pixels / 2).width(2 * pixels);
    $('#sideHistogramDiv').height(2 * pixels).width(pixels / 2);
    $('#cumulativeDiv').height(2 * pixels).width(2 * pixels);
    drawActiveChart();
}

// show and hide data series when asked to
function toggleSeries(index) {
    allCharts = [window.scatterPlot, window.topHistogramPlot, window.sideHistogramPlot, window.histogramPlot, window.cumulativePlot]
    for (index in allCharts){
        chart = allCharts[index]
        if (chart != undefined){
            chart.series[0].toggleDisplay({data: ''});
        }
    }
}

function setScatterX(fieldName) {
    window.scatterXField = fieldName;
    $('.scatterx').css({'cursor':'pointer', 'font-weight':'normal'});
    if (window.scatterxlogOn){
        $('#scatterx' + fieldName + 'log').css({'cursor':'default', 'font-weight':'bold'});
    }
    else{
        $('#scatterx' + fieldName).css({'cursor':'default', 'font-weight':'bold'});
    }
    drawActiveChart();
}

function setScatterY(fieldName) {
    window.scatterYField = fieldName;
    $('.scattery').css({'cursor':'pointer', 'font-weight':'normal'});
    if (window.scatterxlogOn){
        $('#scattery' + fieldName + 'log').css({'cursor':'default', 'font-weight':'bold'});
    }
    else{
        $('#scattery' + fieldName).css({'cursor':'default', 'font-weight':'bold'});
    }
    drawActiveChart();
}

//rearrange the data series so that the one with the specified id is last i.e. on the top layer of the scatter plot
function moveToTop(seriesIndex) {
    allCharts = [window.scatterPlot, window.topHistogramPlot, window.sideHistogramPlot, window.histogramPlot, window.cumulativePlot]
    for (index in allCharts){
        chart = allCharts[index]
        if (chart != undefined){
            console.log('moving to front');
            chart.moveSeriesToFront(seriesIndex);
        }
    }
}

// draw the top and side histograms that accompany the scatter plot
function drawTopSideHistograms() {

    // in case we are being called following a zoom, delete any existing charts
    $('#sideHistogramDiv').empty();
    $('#topHistogramDiv').empty();

    //sanity check - we need to have a scatter plot and a list of data series to proceed
    if (typeof scatterPlot == 'undefined' || typeof contigSetRawData == 'undefined') {
        return;
    }

    var topXMin = scatterPlot.axes.xaxis.min;
    if (window.scatterXField == 'length') {
        topXMin = Math.max(window.minSeqLength, topXMin)
    }

    // build data using the current X field of the scatter plot and the max/min from the already-drawn scatter plot
    var topHistogramData = buildHistogram(window.scatterXField, topXMin, scatterPlot.axes.xaxis.max);

    // decide whether to render a linear or a logarithmic axis
    var topHistogramXaxisRenderer = window.scatterxlogOn ? $.jqplot.LogAxisRenderer : $.jqplot.LinearAxisRenderer;

    // grab the list of colours to use from the series List - they may have been rearranged since last time
    var colourList = contigSetRawData.map(function(a) {
        return a.colour;
    });

    // set up series options - only show lines for series that are enabled
    var mySeriesOptions = [];
    for (var i = 0; i < contigSetRawData.length; i++) {
        mySeriesOptions.push(
                {
                    showLine : window.series[i]
                }
        );
    }

    // now go ahead and create the plot
    topHistogramPlot = $.jqplot('topHistogramDiv',
            topHistogramData,
            {
                seriesColors : colourList,
                seriesDefaults:{
                    showMarker: false,
                    lineWidth: 1
                },
                series: mySeriesOptions,
                axes:{
                    xaxis:{
                        pad: 0,
                        labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                        min : scatterPlot.axes.xaxis.min, // take the max and min from the scatter plot
                        max : scatterPlot.axes.xaxis.max,
                        renderer : topHistogramXaxisRenderer,
                        numberTicks : scatterPlot.axes.xaxis.numberTicks // also try to copy the scatter plot number of ticks

                    },
                    yaxis:{
                        label: 'frequency',
                        pad : 0,
                        labelRenderer: $.jqplot.CanvasAxisLabelRenderer // use this renderer to make the text rotated
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


    // same process for the side histogram
    var sideHistogramYaxisRenderer;


    var sideXMin = scatterPlot.axes.yaxis.min;
    if (window.scatterYField == 'length') {
        sideXMin = Math.max(window.minSeqLength, sideXMin)
    }
    console.log(' side x min is ' + sideXMin);
    // because the plot is on its side, we need to transpose the x and y elements of each data point
    var sideHistogramData = buildHistogram(window.scatterYField, sideXMin, scatterPlot.axes.yaxis.max).map(function(a) {
        return a.map(function(b) {
            return [b[1], b[0]];
        });
    });

    sideHistogramYaxisRenderer = window.scatterylogOn ? $.jqplot.LogAxisRenderer : $.jqplot.LinearAxisRenderer;

    sideHistogramPlot = $.jqplot('sideHistogramDiv',
            sideHistogramData,
            {   sortData : false,  // prevent jqplot from sorting the data - we want the points to go from top to bottom, not left to right
                seriesColors : colourList,
                seriesDefaults:{
                    showMarker: false,
                    lineWidth: 1
                },
                series: mySeriesOptions,
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

// draw whichever chart we want to look at
drawActiveChart = function() {
    // hide all the options and all charts while we are drawing - we want to prevent the user clicking on anything
    $('.chartOptions').hide();
    $('.chartDiv').empty();
    $('#chartBlock').mask('building chart...');

    // now draw the chart with a timeout to make sure that the options get hidden
    if (window.activeChart == 'histogram') {
        setTimeout('drawChart();', 10);
    }

    if (window.activeChart == 'scatterplot') {
        setTimeout('drawScatterChart();', 10);
    }

    if (window.activeChart == 'cumulative') {
        setTimeout('drawCumulativeChart();', 10);
    }

}


drawScatterChart = function() {
    console.log('starting draw scatter');
    var start = new Date().getTime();

    // choose log or linear axes depending on the options
    var yAxisRenderer = window.scatterylogOn ? $.jqplot.LogAxisRenderer : $.jqplot.LinearAxisRenderer;
    var xAxisRenderer = window.scatterxlogOn ? $.jqplot.LogAxisRenderer : $.jqplot.LinearAxisRenderer;


    // map through the data - each series will generate an array of 8-element arrays. We will use the first to elements to plot X and Y and the last six to display the tooltip
    var allValues = [];
    var allDatapointsCount = 0;
    for (var i = 0; i < contigSetRawData.length; i++) {
        var a = contigSetRawData[i];
        var length = a.id.length;
        var result = [];
        if (true) {
            for (var n = 0; n < length; n++) {
                // only add contigs if they pass the length and coverage filters
                if (a.length[n] >= window.minSeqLength && a.coverage[n] >= window.minSeqCoverage) {
                    result.push([a[window.scatterXField][n], a[window.scatterYField][n], a.id[n], a.length[n], a.lengthwithoutn[n], a.quality[n], a.coverage[n], a.gc[n], a.topBlast[n]]);
                    allDatapointsCount++;
                }
            }
        }
        allValues.push(result);
    }
    console.log(allValues);


    var pointSize = 8;
    if (allDatapointsCount > 100) {
        pointSize = 6;
    }
    if (allDatapointsCount > 1000) {
        pointSize = 5;
    }


    // grab the colours
    var colourList = contigSetRawData.map(function(a) {
        return a.colour;
    });

    var mySeriesOptions = [];
    for (var i = 0; i < contigSetRawData.length; i++) {
        mySeriesOptions.push(
                {
                    show : window.series[i],
                    label : contigSetRawData[i].label,
                    trendline: {
                        show: window.scattertrendOn,
                        color: colourList[i],
                        label: '',
                        type: 'linear',
                        shadow: false,
                        lineWidth: 1.5
                    }
                }
        );
    }

    console.log('built options : ' + (new Date().getTime() - start));


    // now create the plot
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
                        size : pointSize
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
                    show: true,
                    tooltipLocation:'ne',
                    sizeAdjust: 7.5,
                    markerRenderer : new $.jqplot.MarkerRenderer({color:'#FFFFFF'}),
                    yvalues: 9,
                    formatString : '%.2f,%.2f<br/>id: %s<br/>length: %d (minus Ns : %d)<br/>quality: %d<br/>coverage: %.2f<br/>gc: %.2f',    // a fragment of HTML that displays all contig info on a tooltip
                    useAxesFormatters: false,
                    bringSeriesToFront: true

                },
                cursor: {
                    show: true,
                    showVerticalLine: true,
                    showHorizontalLine: true,
                    zoom:true
                },
                legend: {
                    renderer: $.jqplot.EnhancedLegendRenderer,
                    show: true
                },
                grid: {
                    background: '#ffffff'
                }
            }
    );
    console.log('drew chart : ' + (new Date().getTime() - start));


    // tell this plot to redraw the top & side histograms whenever it is redrawn (i.e. when the user zooms)
    scatterPlot.postDrawHooks.add(function() {
        drawTopSideHistograms();
    });
    drawTopSideHistograms();

    turnScatterHighlighterOn();

    // all done; hide the spinner and unhide the other stuff
//    $('#spinner').hide();
    $('#chartBlock').unmask();
    $('.chartOptions').show();
    $('.cumulative-options').hide();
    $('.scatterplot-options').show();
    $('.histogram-options').hide();

    $('#scatterplotContainer').show();


}

// utility funtion to build histograms from the raw contig data. Takes the name of a field and the max/min values
buildHistogram = function(fieldName, min, max) {
    console.log('starting build histogram from ' + min + ' to ' + max);
    var start = new Date().getTime();
    // if the max/min hasn't been supplied, work out the overall max/min across all data series
    if (!max) {
        max = Math.max.apply(Math, contigSetRawData.map(function(set) {
            return Math.max.apply(Math, set[fieldName]);
        }));
    }
    if (!min) {
        min = Math.min.apply(Math, contigSetRawData.map(function(set) {
            return Math.min.apply(Math, set[fieldName]);
        }));
    }


    // use a map to iterate over the individual data series
    var allFieldCounts = contigSetRawData.map(function(set) {

        var fieldValues = [];
        for (var i = 0; i < set['length'].length; i++) {

            // only add data points to the collection of values if ...
            if (
            // ...they are longer than the minimum length ...
                    set['length'][i] >= window.minSeqLength &&
                        // ...and have more than the minimum coverage...
                            set.coverage[i] >= window.minSeqCoverage &&
                        // ...and either we are not looking at a scatter plot...
                            (window.activeChart != 'scatterplot' ||
                                    (
                                        // ...or if we are, then only if they fall within the range of the current scatter plot (which may be zoomed)
                                            set[window.scatterXField][i] >= scatterPlot.axes.xaxis.min &&
                                                    set[window.scatterXField][i] <= scatterPlot.axes.xaxis.max &&
                                                    set[window.scatterYField][i] >= scatterPlot.axes.yaxis.min &&
                                                    set[window.scatterYField][i] <= scatterPlot.axes.yaxis.max
                                            )
                                    )
                    ) {
                fieldValues.push(set[fieldName][i]);
            }
        }
        var fieldCounts = [];

        // use some convoluted math to work out the step size - it should be the power of 10 that gives us 100 bins
        var logMax = Math.floor(Math.log(max - min) / Math.LN10);
        var stepSize = Math.max(0.01, Math.pow(10, logMax - 2));
        var numberOfSteps = Math.floor((max / stepSize)) + 2;

        for (var i = (min / stepSize); i <= numberOfSteps; i++) {
            // work out count for a given bin
            var binFloor = i * stepSize;
            var binCeiling = (i + 1) * stepSize;
            var count = fieldValues.filter(
                    function(element) {
                        return (element >= binFloor && element < binCeiling);
                    }).length;
            // if we're going to display this data on a log scale, add 0.1 to each count to avoid log(0) error
            if (logOn) {
                count = count + 0.1;
            }
            // do we want to scale it, to make series with different numbers of contigs easier to compare?
            if (scaledOn) {
                count = 1000 * count / set[fieldName].length;
            }
            fieldCounts.push([binFloor, count]);

        }

        return fieldCounts;
    });
    return allFieldCounts;
}

// draw a histogram
drawChart = function() {

    // pick the correct renderer to use
    var renderer = logOn ? $.jqplot.LogAxisRenderer : $.jqplot.LinearAxisRenderer;

    // grab the colour list
    var colourList = contigSetRawData.map(function(a) {
        return a.colour;
    });

    var mySeriesOptions = [];
    for (var i = 0; i < contigSetRawData.length; i++) {
        mySeriesOptions.push(
                {
                    lineOptions: {
                        showLine : window.series[i]
                    },
                    show: window.series[i],
                    label : contigSetRawData[i].label
                }
        );
    }
    console.log(mySeriesOptions);
    var allFieldValues;
    if (window.chartType == 'length') {
        console.log('type is length, using min seq length');
        allFieldValues = buildHistogram(window.chartType, window.minSeqLength);
    }
    else {
        allFieldValues = buildHistogram(window.chartType);
    }

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
    // show the options again
//    $('#spinner').hide();
    $('#chartBlock').unmask();

    $('.chartOptions').show();
    $('.cumulative-options').hide();
    $('.scatterplot-options').hide();
    $('.histogram-options').show();

}

// draw kumar-blaxter curve for the contig set
drawCumulativeChart = function() {

    // we will need arrays to hold an n50 and n90 value for each series
    var n50values = [];
    var n90values = [];

    // use a map to go through the series
    var allValues = contigSetRawData.map(function(dataset) {

        // are we using length without N?
        var lengthField = window.cumulativefilternOn ? 'lengthwithoutn' : 'length';

        // get an array of all the lengths, filtering by minimum sequence length, and sort them in reverse order i.e. largest first
        var lengths = dataset[lengthField].filter(
                function(element) {
                    return (element >= window.minSeqLength);
                }).sort(function(a, b) {
                    return b - a
                });

        // calculate the sum of all lengths - we need this to work out the n50/n90
        var lengthsSum = 0;
        for (var i = 0; i < lengths.length; i++) {
            lengthsSum += lengths[i];
        }
        var n50Target = lengthsSum / 2
        var n90Target = (lengthsSum / 100) * 90

        // use flags to keep track of whether or not we have seen the n50/n90 contig yet
        var seenn50 = false;
        var seenn90 = false;

        var returnValue = [];
        var cumulativeTotal = 0;

        // go throught the sorted list of lengths and push the cumulative values onto the return array
        for (var i = 0; i < lengths.length; i++) {
            var l = lengths[i];
            cumulativeTotal += l;
            returnValue.push([i, cumulativeTotal, l]);

            // when we hit the n50 contig, push some data onto the n50 array
            if (cumulativeTotal >= n50Target && !seenn50) {
                n50values.push({
                    contigNumber : i,
                    contigLength : l,
                    totalLength : cumulativeTotal
                });
                seenn50 = true;
            }

            // ditto for n90
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


    //grab the colours
    var colourList = contigSetRawData.map(function(a) {
        return a.colour;
    });

    // decide whether or not to show each series
    var mySeriesOptions = [];
    for (var i = 0; i < contigSetRawData.length; i++) {
        mySeriesOptions.push(
                {
                    showLine : window.series[i],
                    showLabel : window.series[i],
                    label : contigSetRawData[i].label

                }
        );
    }

    // go through the list of contig sets again and add a new series for each to hold n50values
    for (var i = 0; i < contigSetRawData.length; i++) {
        allValues.push([
            [n50values[i].contigNumber, n50values[i].totalLength, 'n50 length : ' + n50values[i].contigLength]
        ]);
        // also add a new object to the series options - decide whether or not to show the marker
        mySeriesOptions.push(
                {
                    showMarker: window.series[i],
                    showLabel : false,
                    showLine : false,
                    pointLabels: {
                        show:window.series[i],
                        ypadding : 5,
                        xpadding : 5,
                        location : 'e'
                    }
                }
        );
    }

    // go through the list of contig sets again and add a new series to hold n90values
    for (var i = 0; i < contigSetRawData.length; i++) {
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
                        location : 'e'
                    }
                }
        );
    }

    // now create the plot
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
                        pad : 0
                    }
                },
                highlighter: {
                    show: window.cumulativehighlighterOn,
                    sizeAdjust: 7.5,
                    bringSeriesToFront: true,
                    yvalues: 2,
                    formatString : 'rank : %d<br/>cumulative length : %d<br/>contig length: %d<br/>',
                    useAxesFormatters: false
                },
                cursor: {
                    show: !window.cumulativehighlighterOn,
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

    // done!
//    $('#spinner').hide();
    $('#chartBlock').unmask();
    $('.chartOptions').show();
    $('.cumulative-options').show();
    $('.scatterplot-options').hide();
    $('.histogram-options').hide();


}

//show / hide the different chart types
function switchTo(chartType) {
    console.log('switching to ' + chartType);

    // make the one we've selected bold, and make the others have a pointer
    $('.chartTypeSelector').css({'cursor':'pointer', 'font-weight':'normal'});
    $('#turn' + chartType + 'On').css({'cursor':'default', 'font-weight':'bold'});

    // hide all chart containers, then show the one we want
    $('.chartContainer').hide();
    $('#' + chartType + 'Container').show();

    // delete all charts, show the spinner
    $('.chartDiv').empty();
    $('#spinner').show();

    // set the correct advice chart, and draw it
    window.activeChart = chartType;
    setTimeout('drawActiveChart();', 1);
}


function setHistogramX(fieldName) {
    // make the selected field bold and make all the others clickable
    $('.histogramField').css({'cursor':'pointer', 'font-weight':'normal'});
    $('#turn' + fieldName + 'On').css({'cursor':'default', 'font-weight':'bold'});

    // update the chart type and draw it
    window.chartType = fieldName;
    setTimeout('drawActiveChart();', 1);
}

function turnScatterHighlighterOn() {
    scatterPlot.plugins.highlighter.show = true;
    $('#turnscatterzoomOn').css({'cursor':'pointer', 'font-weight':'normal'});
    $('#turnscatterhighlighterOn').css({'cursor':'default', 'font-weight':'bold'});
}

function turnScatterZoomOn() {
    scatterPlot.plugins.cursor.show = true;
    $('#turnscatterhighlighterOn').css({'cursor':'pointer', 'font-weight':'normal'});
    $('#turnscatterzoomOn').css({'cursor':'default', 'font-weight':'bold'});
}

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

    // global variables that determine how the histogram is drawn
    window.highlighterOn = true;
    window.logOn = false;
    window.scaledOn = false;

    // globals that affect the scatter plot
    window.scatterhighlighterOn = true;
    window.scatterylogOn = true;
    window.scatterxlogOn = false;
    window.scattertrendOn = false;

    // globals that affect the cumulative plot
    window.cumulativehighlighterOn = true


    // global for the histogram field
    window.chartType = 'length';

    // length/coverage filters
    window.minSeqLength = 200;
    window.minSeqCoverage = 0;

    // function to handle the various option toggles

    var setUpToggle = function(variableName) {
        // when the turnsomethingOn element is clicked....
        $('#turn' + variableName + 'On').click(function() {
            // ..set the global variable to true...
            window[variableName + 'On'] = true;
            // ..make the 'on' element bold and the 'off' element clickable...
            $('#turn' + variableName + 'Off').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turn' + variableName + 'On').css({'cursor':'default', 'font-weight':'bold'});
            // ..and redraw the chart
            drawActiveChart();
        });
        $('#turn' + variableName + 'Off').click(function() {
            window[variableName + 'On'] = false;
            $('#turn' + variableName + 'On').css({'cursor':'pointer', 'font-weight':'normal'});
            $('#turn' + variableName + 'Off').css({'cursor':'default', 'font-weight':'bold'});
            drawActiveChart();
        });
    };

    // global toggles that affect all charts
    setUpToggle('highlighter');
    setUpToggle('log');
    setUpToggle('scaled');

    // toggles for scatter plot
    setUpToggle('scatterhighlighter');
    setUpToggle('scattertrend');
    setUpToggle('scatterylog');
    setUpToggle('scatterxlog');

    // toggle for cumulative n filtering
    setUpToggle('cumulativefiltern');
    setUpToggle('cumulativehighlighter');

    // three 'reset zoom' elements
    $('#resetZoom').click(function() {
        scatterPlot.resetZoom();
    });
    $('#resetHistogramZoom').click(function() {
        histogramPlot.resetZoom();
    });
    $('#resetCumulativeZoom').click(function() {
        cumulativePlot.resetZoom();
    });


    // set up some sensible defaults
    setHistogramX('length');
    setScatterX('gc');
    setScatterY('coverage');

    scatterPlot = undefined;
    $('#scatterplotDiv').bind('jqplotDataClick', function (ev, seriesIndex, pointIndex, data) {
        window.location = '../contig/show/' + data[2];
    });

    // do the initial get and draw the first chart

});
