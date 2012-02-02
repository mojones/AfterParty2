<%@ page import="afterparty.StatisticsService; afterparty.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
    <title>Viewing a set of contigs</title>

    %{--we need raphael to draw comparison graphs--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.raphael-min.js')}"></script>
    %{--<script type="text/javascript" src="${resource(dir: 'js', file: 'g.line-min.js')}"></script>--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.line.custom.js')}"></script>




    <script type="text/javascript">

        //         set up ajax compare assemblies
        $(document).ready(function() {

            // we will need some variables that apply to all three charts
            var raphaelWidth = $('#lengthGraphDiv').width() - 40;
            var chartWidth = raphaelWidth - 400;

            // allow arrays to calculate their own maximum value
            Array.prototype.max = function() {
                return Math.max.apply(Math, this);
            };

            // function to draw a chart
            var drawAChart = function(containingElementId, xValues, yValues, colours, log, yMax) {


                var r = Raphael(containingElementId, raphaelWidth, 1000);
                if (log) {
                    var chart = r.g.linechart(100, 100, chartWidth, 600, xValues, yValues, {colors: colours, axis: "0 0 1 0", axisymax: 5});
                    var axis = r.g.axis(
                            85, // distance away from the left side of the canvas
                            600 + 100 - 10, // distance from the top = the chart height + the y-position of the chart - 10 pixels padding
                            600 - 10, // position of the end of the text - probably we want this to be the length of the axis
                            null, // start of the range - leave it as null as we are using our own labels
                            null, // end of the range ditto
                            4, // number of labels we want - 1 (i.e. 0-based index of the last label)
                            1, // orientation: 0 -> x-axis, 1 -> y-axis
                            ['1', '10', '100', '1000', '10000'], // array of labels
                            "|", // the type of tick mark
                            10); // the size of the tick mark
                }
                else {
                    var chart = r.g.linechart(100, 100, chartWidth, 600, xValues, yValues, {colors: colours, axis: "0 0 1 0"});
                    chart.hoverColumn(function() {
                        console.log(this);
//                        r.popup(this.bar.x, this.bar.y, this.bar.value || "0").insertBefore(this);
                    }, function() {
//                        console.log('out');
                    });
                    var maximum = Math.max(yValues);
                    var axis = r.g.axis(
                            85, // distance away from the left side of the canvas
                            600 + 100 - 10, // distance from the top = the chart height + the y-position of the chart - 10 pixels padding
                            600 - 10, // position of the end of the text - probably we want this to be the length of the axis
                            0, // start of the range - leave it as null as we are using our own labels
                            yMax, //Math.max(yValues), // end of the range ditto
                            3, // number of labels we want - 1 (i.e. 0-based index of the last label)
                            1, // orientation: 0 -> x-axis, 1 -> y-axis
                            null, // array of labels
                            "|", // the type of tick mark
                            10); // the size of the tick mark

                }

            };

            $.get('/contigSet/showContigSetsJSON/?idList=${params.idList}', function(data) {

                // utility function - lets us do the equivalent of data.assemblyList*.colour, data.assemblyList*.lengthYvalues, etc as per Groovy
                var extractField = function(fieldname) {
                    return  data.contigSetList.map(function(a) {
                        return a[fieldname];
                    });
                };

                var colours = extractField('colour');

                // draw length chart
                var lengthYvalues = extractField('lengthYvalues');
                var lengthXvalues = extractField('lengthXvalues');
                var lengthYmax = 1000; //data.lengthYmax; // we need the max of the max!
                drawAChart('lengthGraphDiv', lengthXvalues, lengthYvalues, colours, false, lengthYmax);


                if (data.drawQuality) {
                    // draw quality chart
                    var qualityYvalues = extractField('qualityYvalues');
                    var qualityXvalues = extractField('qualityXvalues');
                    var qualityYmax = 1000; //data.qualityYmax;
                    drawAChart('qualityGraphDiv', qualityXvalues, qualityYvalues, colours, false, qualityYmax);
                }
                else{
                    $('qualityGraphDiv').html('<h2>No quality data</h2>')
                }

                if (data.drawCoverage) {
                    // draw coverage chart
                    var coverageYvalues = extractField('coverageYvalues');
                    var coverageXvalues = extractField('coverageXvalues');
                    var coverageYmax = 10000; //data.coverageYmax;
                    drawAChart('coverageGraphDiv', coverageXvalues, coverageYvalues, colours, true, coverageYmax);
                }else{
                    $('coverageGraphDiv').html('<h2>No coverage data</h2>')
                }

                //TODO why does this not work if the quality tab is showing while we are trying to load the charts????

                $('.spinner').hide();
            });


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
            </ul>

            <p>Use the tabs to navigate between charts</p>
        </div>        <!-- .sidebar ends -->

        <div class="sidebar_content" id="sb1_raw">

            <p>Length graph will go here</p>

            <div id="lengthGraphDiv" style="height: 1000px;">
                <h2 class="spinner">Drawing graphs...<img src="${resource(dir: 'images', file: 'spinner.gif')}" style="vertical-align: middle;">
                </h2>
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
        </div>        <!-- .sidebar_content ends -->

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
