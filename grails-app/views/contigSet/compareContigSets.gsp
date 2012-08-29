<%@ page import="afterparty.StatisticsService; afterparty.Study" %>
<html>
<head>
    <meta name="layout" content="main.gsp"/>
    <title>Contig set</title>

    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.jqplot.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.highlighter.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.cursor.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.logAxisRenderer.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.pointLabels.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.trendline.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.canvasAxisLabelRenderer.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.canvasTextRenderer.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'afterpartyCharts.js')}"></script>

    <link rel="stylesheet" href="${resource(dir: 'js', file: 'jquery.jqplot.css')}"/>

    <style type="text/css">
    .jqplot-point-label {
        padding: 1px 3px;
        background-color: #eeeeee;
        font-size: 12px;
    }
    </style>

    <script type="text/javascript">

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

                doCreate(ids, ${contigSets[0].study.id});
            });

            // draw and paginate table first
            <g:if test="${contigSets.size() == 1}">


            setUpEditInPlace(
                    ${contigSets[0].id},
                    "<g:createLink controller="update" action="updateField"/>",
                    'ContigSet'
            );

//            $('#contigTable').tablesorter({debug:true});
//
//            $("#contigTable").bind("sortStart", function() {
//                $('#contigTable').mask('Sorting...');
//            });
//            $("#contigTable").bind("sortEnd", function() {
//                $('#contigTable').unmask();
//                updatePaginator();
//            });


            </g:if>


            $('#chartBlock').mask('downloading data...');
            $.get('/contigSet/showContigSetsJSON/?idList=${contigSets*.id.join(',')}', function(data) {
                contigSetRawData = data;
                // start by showing the user the scatter plot
                $('#downloadingSpinner').hide();
                switchTo('histogram');

            });
        });
    </script>

</head>

<body>
<div class="row-fluid">
    <div class="span10 offset1">
        <g:if test="${contigSets.size() == 1}">
            <g:set var="contigSetInstance" value="${contigSets[0]}"/>

            <h2>Contig Set details</h2>
            <h3>Name</h3>
            <p class="edit_in_place" name="name">
                <g:if test="${isOwner}">
                    <i class="icon-pencil"></i>&nbsp;
                </g:if>
                ${contigSetInstance.name}
            </p>
            <h3>Description</h3>
            <p class="edit_in_place" name="description">
                <g:if test="${isOwner}">
                    <i class="icon-pencil"></i>&nbsp;
                </g:if>
                ${contigSetInstance.description}
            </p>

            <g:render template="/contigSet/searchForm" model="['contigSetId' : contigSetInstance.id, 'readSources' : readSources]"/>

            <br/>
            <g:form controller="contigSet" action="download" method="get">
                <g:hiddenField name="id" value="${contigSetInstance.id}"/>
                <button type="submit" class="btn btn-info">
                    <i class="icon-download-alt"></i>&nbsp;download contigs
                </button>
            </g:form>

            <h2>Contigs in this set</h2>
            <g:render template="contigTable" model="['contigCollection' : contigData[0], 'contigsPerPage' : 10]"/>

        </g:if>
        <g:else>
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
                                        ${contigSet.name} &nbsp;&nbsp;
                                        <span style="cursor:pointer;" onclick="toggleSeries(${index});">toggle</span> |
                                        <span style="cursor:pointer;" onclick="moveToTop(${index});">move to top</span>
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
        </g:else>

        <h2>Contig Set charts</h2>

        <p class="chartOptions">Chart type :
            <span class="chartTypeSelector" id='turnscatterplotOn' onclick="switchTo('scatterplot')">scatter plot</span> |
            <span class="chartTypeSelector" id='turnhistogramOn' onclick="switchTo('histogram')">histogram</span> |
            <span class="chartTypeSelector" id='turncumulativeOn' onclick="switchTo('cumulative')">cumulative length</span>
        </p>

        <p class="chartOptions">
            Chart size :
            <span onclick="setChartSize(200);" style="cursor: pointer;">tiny</span> |
            <span onclick="setChartSize(300);" style="cursor: pointer;">small</span> |
            <span onclick="setChartSize(400);" style="cursor: pointer;">medium</span> |
            <span onclick="setChartSize(600);" style="cursor: pointer;">large</span> |
            <span onclick="setChartSize(800);" style="cursor: pointer;">huge</span>
        </p>

        <p class="chartOptions">
            Minimum sequence length: <input value="200" type="text" id="minimumSequenceLength"/> <input type="submit" value="filter" onclick="window.minSeqLength = ($('#minimumSequenceLength').val());
        drawActiveChart();">
            <br/>
            Minimum sequence coverage: <input type="text" id="minimumSequenceCoverage"/> <input type="submit" value="filter" onclick="window.minSeqCoverage = ($('#minimumSequenceCoverage').val());
        drawActiveChart();">
        </p>

        <p class="chartOptions">Exclude Ns from length : <span id='turncumulativefilternOff' style="font-weight: bold;">no</span> |
            <span style="cursor: pointer; " id='turncumulativefilternOn'>yes</span>
        </p>


        <div class="chartContainer" id='histogramContainer'>
            <p class="chartOptions">Mouseover : <span id='turnhighlighterOn' style="font-weight: bold;">highlight</span> |
                <span style="cursor: pointer; " id='turnhighlighterOff'>zoom</span> (
                <span style="cursor: pointer;" id="resetHistogramZoom">click to reset</span>)

            </p>

            <p class="chartOptions">

                Y axis : <span id='turnlogOn' style="cursor: pointer; ">log</span> | <span style="font-weight: bold;" id='turnlogOff'>linear</span>
                &nbsp;&nbsp;&nbsp;

                Scale : <span id='turnscaledOn' style="cursor: pointer; ">per 1000 contigs</span> | <span style="font-weight: bold;" id='turnscaledOff'>raw frequency</span>
            </p>

            <p class="chartOptions">
                Chart type :
                <span onclick="setHistogramX('length')" id='turnlengthOn' class="histogramField">length</span> |
                <span onclick="setHistogramX('quality')" id='turnqualityOn' class="histogramField">quality</span> |
                <span onclick="setHistogramX('coverage')" id='turncoverageOn' class="histogramField">coverage</span>|
                <span onclick="setHistogramX('gc')" id='turngcOn' class="histogramField">gc</span>
            </p>


            <div class="chartDiv" id="histogramDiv" style="height: 400px; width: 800px;">

            </div>
        </div>

        <div class="chartContainer" id='cumulativeContainer'>
            <p class="chartOptions">Mouseover : <span id='turncumulativehighlighterOn' style="font-weight: bold;">highlight</span> |
                <span style="cursor: pointer; " id='turncumulativehighlighterOff'>zoom</span> (
                <span style="cursor: pointer;" id="resetCumulativeZoom">click to reset</span>)

            </p>


            <div class="chartDiv" id="cumulativeDiv" style="height: 800px; width: 1000px;">

            </div>
        </div>

        <div class="chartContainer" id='scatterplotContainer'>
            <p class="chartOptions" class='scatterplotOptions'>
                <span style="cursor: pointer;" id="resetZoom">click to reset zoom</span> , <span style="cursor: pointer;" id="saveSelected">click to save selected</span>)
            &nbsp;&nbsp;&nbsp;

            trendlines : <span id='turnscattertrendOff' style="font-weight: bold;">off</span> | <span id='turnscattertrendOn' style="cursor: pointer;">on</span>

                &nbsp;&nbsp;&nbsp;

                Y axis : <span id='turnscatterylogOn' style="font-weight: bold;">log</span> | <span style="cursor: pointer; " id='turnscatterylogOff'>linear</span>
                &nbsp;&nbsp;&nbsp;
                X axis : <span id='turnscatterxlogOn' style="cursor: pointer; ">log</span> | <span style="font-weight: bold;" id='turnscatterxlogOff'>linear</span>
            </p>

            <p class="chartOptions" class='scatterplotOptions'>
                X axis :
                <span class="scatterx" id="scatterxlength" style="cursor: pointer; " onclick="setScatterX('length');">length</span> |
                <span class="scatterx" id="scatterxlengthwithoutn" style="cursor: pointer; " onclick="setScatterX('lengthwithoutn');">length excluding N</span> |
                <span class="scatterx" id="scatterxquality" style="cursor: pointer; " onclick="setScatterX('quality');">quality</span> |
                <span class="scatterx" id="scatterxcoverage" style="cursor: pointer; " onclick="setScatterX('coverage');">coverage</span> |
                <span class="scatterx" id="scatterxgc" style="font-weight: bold;" onclick="setScatterX('gc');">gc</span>

            </p>

            <p class="chartOptions" class='scatterplotOptions'>
                Y axis :
                <span class="scattery" id="scatterylength" style="cursor: pointer; " onclick="setScatterY('length');">length</span> |
                <span class="scattery" id="scatterylengthwithoutn" style="cursor: pointer; " onclick="setScatterY('lengthwithoutn');">length excluding N</span> |
                <span class="scattery" id="scatteryquality" style="cursor: pointer; " onclick="setScatterY('quality');">quality</span> |
                <span class="scattery" id="scatterycoverage" style="font-weight: bold;" onclick="setScatterY('coverage');">coverage</span> |
                <span class="scattery" id="scatterygc" style="cursor: pointer; " onclick="setScatterY('gc');">gc</span>

            </p>

            <table>
                <tr>
                    <td style="border: none; margin-left: 10px;">
                        <div class="chartDiv" id="topHistogramDiv" style="height: 200px; width: 800px;"></div>
                    </td>
                    <td style="border: none;">

                    </td>
                </tr>
                <tr>
                    <td style="border: none;">
                        <div class="chartDiv" id="scatterplotDiv" style="height: 800px; width: 800px;"></div>
                    </td>
                    <td style="border: none;">
                        <div class="chartDiv" id="sideHistogramDiv" style="height: 800px; width: 200px;"></div>
                    </td>
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
