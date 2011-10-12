<%@ page import="afterparty.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
    <title>Dataset List</title>
</head>

<body>

<g:each in="${studyInstanceList}" status="i" var="studyInstance">

    <div class="block withsidebar">

        <div class="block_head">
            <div class="bheadl"></div>

            <div class="bheadr"></div>

            <h2><g:link action="show"
                id="${studyInstance.id}">${studyInstance.name}</g:link></h2>
        </div>        <!-- .block_head ends -->



        <div class="block_content">

            <div class="sidebar">
                <ul class="sidemenu">
                    <li><a href="#sb1_${studyInstance.id}">Description</a></li>
                    <li><a href="#sb2_${studyInstance.id}">Statistics</a></li>
                    <li><a href="#sb3_${studyInstance.id}">Annotation</a></li>
                </ul>

                <p>Click the study name to see details. Use the <strong>Statistics</strong> tab to view statistics.
                </p>
            </div>        <!-- .sidebar ends -->

            <div class="sidebar_content" id="sb1_${studyInstance.id}">

                <p>${studyInstance.description}</p>

            </div>        <!-- .sidebar_content ends -->


            <div class="sidebar_content" id="sb2_${studyInstance.id}">
                 <p>Samples:${studyInstance.samples.size()}</p>
                 <p>Assemblies:${studyInstance.assemblies.size()}</p>
                 <p>Read count:${studyInstance.getRawReadsCount()}</p>
            </div>        <!-- .sidebar_content ends -->


            <div class="sidebar_content" id="sb3_${studyInstance.id}">
                <h3>Sidebar content 3</h3>

                <p>Aenean facilisis ligula eget orci adipiscing varius. Curabitur sem ligula, egestas vel bibendum sed, sodales eu nulla. Vestibulum luctus aliquam feugiat. Donec porta interdum placerat. Donec velit enim, porta vitae euismod ut, fermentum eu felis. Morbi aliquet, libero vel gravida facilisis, enim risus consequat tellus, vitae luctus est diam non nisi. Vivamus eget leo sit amet neque ultricies blandit. Sed tristique erat a sem ullamcorper tempor. Curabitur turpis lorem, semper et pharetra in, scelerisque in magna. Ut at tortor sed diam mattis rhoncus vel eget turpis. Praesent id diam velit, ullamcorper semper augue. Curabitur at orci tellus, sed tincidunt enim. Vivamus sed dolor vitae purus dignissim luctus quis sed nunc. Sed urna enim, auctor sit amet volutpat ut, porta sed leo. Integer dictum molestie elementum. Nullam dapibus tempus enim, id tincidunt arcu elementum varius.</p>

                <p>Integer malesuada posuere nibh, ac commodo eros dictum eget. Maecenas vel urna ac sapien posuere tincidunt vel non metus. Morbi accumsan lectus at ante scelerisque molestie. Pellentesque in quam arcu, in lacinia orci. Sed blandit, neque sed ullamcorper lacinia, velit lectus lacinia lorem, id gravida sem arcu vel purus. Aenean tellus massa, elementum id condimentum ut, tempus ac dui. Integer consectetur neque placerat leo adipiscing iaculis. Vivamus tempor dui eu augue malesuada dignissim. In tempor odio eu augue ultricies ut hendrerit.</p>
            </div>        <!-- .sidebar_content ends -->

        </div>        <!-- .block_content ends -->


        <div class="bendl"></div>

        <div class="bendr"></div>

    </div>        <!-- .block ends -->

    </tr>
</g:each>

</body>
</html>
