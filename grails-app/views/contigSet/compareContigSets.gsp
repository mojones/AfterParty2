<%@ page import="afterparty.StatisticsService; afterparty.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
    <title>Viewing a set of contigs</title>

    %{--we need raphael to draw comparison graphs--}%
    %{--<script type="text/javascript" src="${resource(dir: 'js', file: 'raphael-min.js')}"></script>--}%
    %{--<script type="text/javascript" src="${resource(dir: 'js', file: 'g.raphael-min.js')}"></script>--}%
    %{--<script type="text/javascript" src="${resource(dir: 'js', file: 'g.line-min.js')}"></script>--}%
    %{--<script type="text/javascript" src="${resource(dir: 'js', file: 'g.line.custom.js')}"></script>--}%


    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.jqplot.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.highlighter.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.cursor.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jqplot.logAxisRenderer.js')}"></script>
    <link rel="stylesheet" href="${resource(dir: 'js', file: 'jquery.jqplot.css')}"/>





    <script type="text/javascript">

        //         set up ajax compare assemblies
        $(document).ready(function() {

            // global variables that determine how the chart is drawn
            highlighterOn = false;
            cursorOn = false;
            logOn = false;

            // boring code to handle chart options
            $('#turnHighlighterOn').click(function() {
                highlighterOn = true;
                $('#turnHighlighterOff').css({'cursor':'pointer', 'font-weight':'normal'});
                $('#turnHighlighterOn').css({'cursor':'default', 'font-weight':'bold'});
                drawChart();
            });
            $('#turnHighlighterOff').click(function() {
                highlighterOn = false;
                $('#turnHighlighterOn').css({'cursor':'pointer', 'font-weight':'normal'});
                $('#turnHighlighterOff').css({'cursor':'default', 'font-weight':'bold'});
                drawChart();
            });

            $('#turnCursorOn').click(function() {
                cursorOn = true;
                $('#turnCursorOn').css({'cursor':'default', 'font-weight':'bold'});
                $('#turnCursorOff').css({'cursor':'pointer', 'font-weight':'normal'});
                drawChart();
            });
            $('#turnCursorOff').click(function() {
                cursorOn = false;
                $('#turnCursorOff').css({'cursor':'default', 'font-weight':'bold'});
                $('#turnCursorOn').css({'cursor':'pointer', 'font-weight':'normal'});
                drawChart();
            });

            $('#turnLogOn').click(function() {
                logOn = true;
                $('#turnLogOn').css({'cursor':'default', 'font-weight':'bold'});
                $('#turnLogOff').css({'cursor':'pointer', 'font-weight':'normal'});
                drawChart();
            });
            $('#turnLogOff').click(function() {
                logOn = false;
                $('#turnLogOff').css({'cursor':'default', 'font-weight':'bold'});
                $('#turnLogOn').css({'cursor':'pointer', 'font-weight':'normal'});
                drawChart();
            });

            $.get('/contigSet/showContigSetsJSON/?idList=${contigSets*.id.join(',')}', function(data) {
                contigSetData = data;
                drawChart();


            });


            var drawChart = function() {
                $('#lengthGraphDiv').empty();

                $('.spinner').show();

                var allLengthValues;
                var renderer;

                if (logOn) {
                    // if we are plotting on a log scale then we will add 0.1 to all the Y values to prevent log(0) error
                    allLengthValues = contigSetData.contigSetList.map(function(a) {
                        return a.lengthvalues.map(function(b) {
                            return [
                                b[0], b[1] + 0.1
                            ];
                        });
                    });
                    renderer = $.jqplot.LogAxisRenderer;
                } else {
                    allLengthValues = contigSetData.contigSetList.map(function(a) {
                        return a.lengthvalues;
                    });
                    renderer = $.jqplot.LinearAxisRenderer;
                }

                $.jqplot('lengthGraphDiv',
                        allLengthValues,
                        {
                            title: 'length histogram',
                            seriesDefaults:{
                                showMarker: false
                            },
                            axes:{
                                xaxis:{
                                    label:'Length (bases)',
                                    pad: 0
                                },
                                yaxis:{
                                    label:'Frequency',
                                    pad : 0,
                                    renderer: renderer

                                }
                            },
                            highlighter: {
                                show: highlighterOn,
                                sizeAdjust: 7.5
                            },
                            cursor: {
                                show: cursorOn,
                                tooltipLocation:'sw',
                                followMouse : true,
                                showVerticalLine: true,
                                showHorizontalLine: true
                            }
                        }
                );
                $('.spinner').hide();

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
                <th>Contig Set name</th>
                <th>Number of Contigs</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${contigSets}" var="contigSet" status="index">
                <tr style="background-color: ${StatisticsService.paleAssemblyColours[index]}">
                    <td>${contigSet.name}</td>
                    <td>${contigSet.contigs.size()}</td>
                </tr>
            </g:each>
            </tbody>

        </table>

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


<div class="block withsidebar">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Contig Set charts</h2>

    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <div class="sidebar">
            <ul class="sidemenu">
                <li><a href="#sb1_raw">Length</a></li>
                <li><a href="#sb2_raw">Quality</a></li>
                <li><a href="#sb3_raw">Coverage</a></li>
                <li><a href="#sb4_raw">Scaled length</a></li>
                <li><a href="#sb5_raw">Scaled quality</a></li>
                <li><a href="#sb6_raw">Scaled coverage</a></li>
            </ul>

            <p>Use the tabs to navigate between charts</p>
        </div>        <!-- .sidebar ends -->

        <div class="sidebar_content" id="sb1_raw">

            <p>Highlighter : <span id='turnHighlighterOn' style="cursor: pointer; ">on</span> | <span style="font-weight: bold;" id='turnHighlighterOff'>off</span>
            </p>

            <p>Cursor : <span id='turnCursorOn' style="cursor: pointer; ">on</span> | <span style="font-weight: bold;" id='turnCursorOff'>off</span>
            </p>

            <p>Scale : <span id='turnLogOn' style="cursor: pointer; ">log</span> | <span style="font-weight: bold;" id='turnLogOff'>linear</span>
            </p>

            <h2 class="spinner">Drawing graphs...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
            </h2>

            <div id="lengthGraphDiv" style="height: 800px; width: 1000px;">

            </div>
        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb2_raw">
            <p>Quality graph will go here</p>

            <div id="qualityGraphDiv" style="height: 1000px;">
                <h2 class="spinner">Drawing graphs...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                </h2>
            </div>
        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb3_raw">
            <p>Coverage graph will go here</p>

            <div id="coverageGraphDiv" style="height: 1000px;">
                <h2 class="spinner">Drawing graphs...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                </h2>
            </div>
        </div>

        <div class="sidebar_content" id="sb4_raw">
            <p>Coverage graph will go here</p>

            <div id="scaledLengthGraphDiv" style="height: 1000px;">
                <h2 class="spinner">Drawing graphs...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                </h2>
            </div>
        </div>

        <div class="sidebar_content" id="sb5_raw">
            <p>Coverage graph will go here</p>

            <div id="scaledQualityGraphDiv" style="height: 1000px;">
                <h2 class="spinner">Drawing graphs...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                </h2>
            </div>
        </div>

        <div class="sidebar_content" id="sb6_raw">
            <p>Coverage graph will go here</p>

            <div id="scaledCoverageGraphDiv" style="height: 1000px;">
                <h2 class="spinner">Drawing graphs...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                </h2>
            </div>
        </div>        <!-- .sidebar_content ends -->

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
