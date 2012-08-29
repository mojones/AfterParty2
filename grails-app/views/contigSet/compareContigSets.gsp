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
            

            <h2>Contigs in this set</h2>
            <g:form controller="contigSet" action="download" method="get">
                <g:hiddenField name="id" value="${contigSetInstance.id}"/>
                <button type="submit" class="btn btn-info">
                    <i class="icon-download-alt"></i>&nbsp;download contigs
                </button>
            </g:form>
            <g:render template="contigTable" model="['contigCollection' : contigData[0], 'contigsPerPage' : 10]"/>

        </g:if>
        <g:else>
            <h2>Contig sets</h2>
            <table class="table table-bordered table-hover">
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
        </g:else>
    </div>
</div>
<div class="row-fluid">
    <div class="span10 offset1">

        <h2>Contig Set charts</h2>

        <p class="chartOptions">
            <div class="btn-toolbar">
                <div class="btn-group">
                    <a class="btn btn-info dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-signal"></i>&nbsp;chart type
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="chartTypeSelector" id='turnscatterplotOn' onclick="switchTo('scatterplot')">scatter plot</a> </li>
                        <li><a class="chartTypeSelector" id='turnhistogramOn' onclick="switchTo('histogram')">histogram</a> </li>
                        <li><a class="chartTypeSelector" id='turncumulativeOn' onclick="switchTo('cumulative')">cumulative length</a></li>
                    </ul>
                </div>
                
                <div class="btn-group">

                    <a class="btn btn-info dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-resize-full"></i>&nbsp;chart size
                        <span class="caret"></span>
                    </a>    
                    <ul class="dropdown-menu">
                        <li> <a onclick="setChartSize(200);" style="cursor: pointer;">tiny</a></li>
                        <li> <a onclick="setChartSize(300);" style="cursor: pointer;">small</a></li>
                        <li> <a onclick="setChartSize(400);" style="cursor: pointer;">medium</a></li>
                        <li> <a onclick="setChartSize(600);" style="cursor: pointer;">large</a></li>
                        <li> <a onclick="setChartSize(800);" style="cursor: pointer;">huge</a></li>
                    </ul>
                </div>
            </div>
            <div class="btn-toolbar">

                <div class="btn-group histogram-options">
                    <a class="btn btn-warning dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-screenshot"></i>&nbsp;cursor function
                        <span class="caret"></span>
                    </a>    
                    <ul class="dropdown-menu">
                        <li><a id='turnhighlighterOn' style="font-weight: bold;">highlight</a></li>
                        <li><a style="cursor: pointer; " id='turnhighlighterOff'>zoom</a></li>
                    </ul>
                </div>


                <div class="btn-group histogram-options">
                    <a class="btn btn-warning dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-resize-vertical"></i>&nbsp;y-axis scale
                        <span class="caret"></span>
                    </a>    
                    <ul class="dropdown-menu">
                        <li><a id='turnlogOn' style="cursor: pointer; ">log</a></li>
                        <li><a style="font-weight: bold;" id='turnlogOff'>linear</a></li>
                    </ul>
                </div>
                
                <div class="btn-group histogram-options">
                    <a class="btn btn-warning dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-circle-arrow-up"></i>&nbsp;frequency scaling
                        <span class="caret"></span>
                    </a>    
                    <ul class="dropdown-menu">
                        <li><a id='turnscaledOn' style="cursor: pointer; ">per 1000 contigs</a></li>
                        <li><a style="font-weight: bold;" id='turnscaledOff'>raw frequency</a></li>
                    </ul>
                </div>

                <div class="btn-group histogram-options">
                    <a class="btn btn-warning dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-resize-horizontal"></i>&nbsp;x-axis displays
                        <span class="caret"></span>
                    </a>    
                    <ul class="dropdown-menu">
                        <li><a onclick="setHistogramX('length')" id='turnlengthOn' class="histogramField">length</a></li>
                        <li><a onclick="setHistogramX('lengthwithoutn')" id='turnlengthOn' class="histogramField">length excluding Ns</a></li>
                        <li><a onclick="setHistogramX('quality')" id='turnqualityOn' class="histogramField">quality</a></li>
                        <li><a onclick="setHistogramX('coverage')" id='turncoverageOn' class="histogramField">coverage</a></li>
                        <li><a onclick="setHistogramX('gc')" id='turngcOn' class="histogramField">gc</a></li>
                    </ul>
                </div>
                <button class="btn btn-danger histogram-options" style="cursor: pointer;" id="resetHistogramZoom"><i class="icon-refresh"></i>&nbsp;reset zoom</button>
              
                <div class="btn-group cumulative-options">
                    <a class="btn btn-warning dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-screenshot"></i>&nbsp;cursor function
                        <span class="caret"></span>
                    </a>    
                    <ul class="dropdown-menu">
                        <li><a id='turncumulativehighlighterOn' style="font-weight: bold;">highlight</a></li>
                        <li><a style="cursor: pointer; " id='turncumulativehighlighterOff'>zoom</a></li>
                        <li></li>
                    </ul>
                </div>

                <button class="btn btn-danger cumulative-options" style="cursor: pointer;" id="resetCumulativeZoom"><i class="icon-refresh"></i>&nbsp;reset zoom</button>

                <div class="btn-group scatterplot-options">
                    <a class="btn btn-warning dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-resize-horizontal"></i>&nbsp;x-axis display
                        <span class="caret"></span>
                    </a>    
                    <ul class="dropdown-menu">
                        <li><a href="#"><em>Linear</em></a></li>
                        <li><a class="scatterx" id="scatterxlength" style="cursor: pointer; " onclick="window.scatterxlogOn=false;setScatterX('length');">length</a></li>
                        <li><a class="scatterx" id="scatterxlengthwithoutn" style="cursor: pointer; " onclick="window.scatterxlogOn=false;setScatterX('lengthwithoutn');">length excluding N</a></li>
                        <li><a class="scatterx" id="scatterxquality" style="cursor: pointer; " onclick="window.scatterxlogOn=false;setScatterX('quality');">quality</a></li>
                        <li><a class="scatterx" id="scatterxcoverage" style="cursor: pointer; " onclick="window.scatterxlogOn=false;setScatterX('coverage');">coverage</a></li>
                        <li><a class="scatterx" id="scatterxgc" style="font-weight: bold;" onclick="window.scatterxlogOn=false;setScatterX('gc');">gc</a></li>
                        <li class="divider"></li>
                        <li><a href="#"><em>Logarithmic</em></a></li>
                        <li><a class="scatterx" id="scatterxlengthlog" style="cursor: pointer; " onclick="window.scatterxlogOn=true;setScatterX('length');">length</a></li>
                        <li><a class="scatterx" id="scatterxlengthwithoutnlog" style="cursor: pointer; " onclick="window.scatterxlogOn=true;setScatterX('lengthwithoutn');">length excluding N</a></li>
                        <li><a class="scatterx" id="scatterxqualitylog" style="cursor: pointer; " onclick="window.scatterxlogOn=true;setScatterX('quality');">quality</a></li>
                        <li><a class="scatterx" id="scatterxcoveragelog" style="cursor: pointer; " onclick="window.scatterxlogOn=true;setScatterX('coverage');">coverage</a></li>
                        <li><a class="scatterx" id="scatterxgclog" style="font-weight: bold;" onclick="window.scatterxlogOn=true;setScatterX('gc');">gc</a></li>
                        
                    </ul>
                </div>

                <div class="btn-group scatterplot-options">
                    <a class="btn btn-warning dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-resize-vertical"></i>&nbsp;y-axis display
                        <span class="caret"></span>
                    </a>    
                    <ul class="dropdown-menu">
                        <li><a href="#"><em>Linear</em></a></li>
                        <li><a class="scattery" id="scatterylength" style="cursor: pointer; " onclick="window.scatterylogOn=false;setScatterY('length');">length</a></li>
                        <li><a class="scattery" id="scatterylengthwithoutn" style="cursor: pointer; " onclick="window.scatterylogOn=false;setScatterY('lengthwithoutn');">length excluding N</a></li>
                        <li><a class="scattery" id="scatteryquality" style="cursor: pointer; " onclick="window.scatterylogOn=false;setScatterY('quality');">quality</a></li>
                        <li><a class="scattery" id="scatterycoverage" style="cursor: pointer; " onclick="window.scatterylogOn=false;setScatterY('coverage');">coverage</a></li>
                        <li><a class="scattery" id="scatterygc" style="font-weight: bold;" onclick="window.scatterylogOn=false;setScatterY('gc');">gc</a></li>
                        <li class="divider"></li>
                        <li><a href="#"><em>Logarithmic</em></a></li>
                        <li><a class="scattery" id="scatterylengthlog" style="cursor: pointer; " onclick="window.scatterylogOn=true;setScatterY('length');">length</a></li>
                        <li><a class="scattery" id="scatterylengthwithoutnlog" style="cursor: pointer; " onclick="window.scatterylogOn=true;setScatterY('lengthwithoutn');">length excluding N</a></li>
                        <li><a class="scattery" id="scatteryqualitylog" style="cursor: pointer; " onclick="window.scatterylogOn=true;setScatterY('quality');">quality</a></li>
                        <li><a class="scattery" id="scatterycoveragelog" style="cursor: pointer; " onclick="window.scatterylogOn=true;setScatterY('coverage');">coverage</a></li>
                        <li><a class="scattery" id="scatterygclog" style="font-weight: bold;" onclick="window.scatterylogOn=true;setScatterY('gc');">gc</a></li>
                        
                    </ul>
                </div>

                <div class="btn-group scatterplot-options">
                    <a class="btn btn-warning dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-chevron-down"></i>&nbsp;trend lines
                        <span class="caret"></span>
                    </a>    
                    <ul class="dropdown-menu">
                        <li><a id='turnscattertrendOff' style="font-weight: bold;">off</a></li>
                        <li><a id='turnscattertrendOn' style="cursor: pointer;">on</a></li>
                    </ul>
                </div>

                <button class="btn btn-danger scatterplot-options" style="cursor: pointer;" id="resetZoom"><i class="icon-refresh"></i>&nbsp;reset zoom</button>
                <button class="btn btn-success scatterplot-options" style="cursor: pointer;" id="saveSelected"><i class="icon-tags"></i>&nbsp;save as contig set</button>

            </div>

            <div class="input-append">
                <label>Minimum sequence length</label>
                <input placeholder="200" type="text" id="minimumSequenceLength"/> 
                <button class="btn" onclick="window.minSeqLength = ($('#minimumSequenceLength').val()); drawActiveChart();">
                    <i class="icon-filter"></i>&nbsp;Filter
                </button>
            </div>

            <div class="input-append">
                <label>Minimum sequence coverage</label>
                <input placeholder="20" type="text" id="minimumSequenceCoverage"/> 
                <button class="btn" onclick="window.minSeqCoverage = ($('#minimumSequenceCoverage').val()); drawActiveChart();"><i class="icon-filter"></i>&nbsp;Filter</button>
            </div>
        </p>
        <div class="chartContainer" id='histogramContainer'>
            <div class="chartDiv" id="histogramDiv" style="height: 400px; width: 800px;">
            </div>
        </div>

        <div class="chartContainer" id='cumulativeContainer'>
            <div class="chartDiv" id="cumulativeDiv" style="height: 800px; width: 1000px;">
            </div>
        </div>

        <div class="chartContainer" id='scatterplotContainer'>
           
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
        </div>
    </div>
</div>
        

</body>
</html>
