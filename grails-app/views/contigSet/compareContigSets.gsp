<%@ page import="afterparty.StatisticsService; afterparty.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="standalone.gsp"/>
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
    <title>Standalone assembly viewer</title>

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
            $.get('/contigSet/showContigSetsJSON/?idList=${contigSets*.id.join(',')}', function(data) {
                contigSetRawData = data;
                // start by showing the user the scatter plot
                switchTo('histogram');

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
            <g:if test="${isOwner}">
                <p>
                    <g:form action="buildBlastDatabase" method="get">
                        <g:hiddenField name="id" value="${contigSetInstance.id}"/>
                        <input type="submit" class="submit long" value="Make BLASTable"/>
                    </g:form>
                </p>
            </g:if>

            <g:if test="${contigSetInstance.blastHeaderFile}">

                <g:form action="blastAgainstContigSet" method="get">
                    <g:hiddenField name="id" value="${contigSetInstance.id}"/>
                    <p><textarea rows="5" cols="80" name="inputSequence"></textarea></p>

                    <p><input type="submit" class="submit long" value="Search contig set"/></p>
                </g:form>

            </g:if>
        </div>        <!-- .block_content ends -->

        <div class="bendl"></div>

        <div class="bendr"></div>
    </div>

    <g:if test="${contigSetInstance.contigs.size() > 4}">

        <div class="block">
            <div class="block_head">
                <div class="bheadl"></div>

                <div class="bheadr"></div>

                <h2>Contigs in this set</h2>
            </div>        <!-- .block_head ends -->

            <div class="block_content">
                <table cellpadding="0" cellspacing="0" width="100%" class="sortable">

                    <thead>
                    <tr>

                        <th>Contig ID</th>
                        <th>Length</th>
                        <th>Reads</th>
                    </tr>
                    </thead>

                    <tbody>
                    <g:each var="contig" in="${contigSetInstance.contigs.toArray()[0..5]}" status="index">

                        <tr>
                            <td><g:link controller="contig" action="show" id="${contig.id}">${contig.name}</g:link></td>
                            <td>${contig.length()}</td>
                            <td>${contig.reads.size()}</td>

                        </tr>
                    </g:each>
                    </tbody>

                </table>

            </div>        <!-- .block_content ends -->
            <div class="bendl"></div>

            <div class="bendr"></div>
        </div>
    </g:if>
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
                            ${contigSet.label} &nbsp;&nbsp;
                            <span style="cursor:pointer;" onclick="toggleSeries(${index});">toggle</span> |
                            <span style="cursor:pointer;" onclick="moveToTop(${index});">move to top</span>
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
</g:else>
<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Contig Set charts</h2>

    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <h2 id="spinner" style="font-size: 5em;text-align: center;padding-top: 100px;">Drawing chart, please wait...</h2>

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
                <span style="cursor: pointer;" id="resetZoom">click to reset zoom</span>
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
