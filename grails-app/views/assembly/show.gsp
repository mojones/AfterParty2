<%@ page import="afterparty.Assembly" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'assembly.label', default: 'Assembly')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>


    <script type="text/javascript">


        $(document).ready(function() {


            setUpEditInPlace(
                    ${assemblyInstance.id},
                    "<g:createLink controller="update" action="updateField"/>",
                    'Assembly'
            );

            $("#scatterplotForm").submit(function() {
                $('#loadingScatterplotImage').show();
                var xLabel = $('#x_label').val();
                var yLabel = $('#y_label').val();
                var cutoff = $('#cutoff').val();
                if (cutoff == "") {
                    cutoff = 0;
                }
                var colourBy = $('#contig_colour').val();
                $('#scatterplotImage')
                        .attr("src", "<g:createLink controller="assembly" action="scatterplotAjax"/>" + "?assemblyId=${assemblyInstance.id}&x=" + xLabel + "&y=" + yLabel + "&time=" + new Date().getTime() + "&cutoff=" + cutoff + "&colour=" + colourBy)
                        .load(function() {
                            $('#loadingScatterplotImage').hide();
                        })
                        ;
                return false;
            })

            $("#histogramForm").submit(function() {
                $('#loadingHistogramImage').show();
                var xLabel = $('#histogramField').val();
                var scale = $('#histogramScale').val()
                $('#histogramImage')
                        .attr("src", "<g:createLink controller="assembly" action="histogramAjax"/>" + "?assemblyId=${assemblyInstance.id}&x=" + xLabel + "&scale=" + scale + "&time=" + new Date().getTime())
                        .load(function() {
                            $('#loadingHistogramImage').hide();
                        })
                        ;
                return false;
            })


        });



    </script>

    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
</head>

<body>



<div class="block withsidebar">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Assembly details</h2>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <div class="sidebar">
            <ul class="sidemenu">
                <li><a href="#sb1_raw">Description</a></li>
                <li><a href="#sb2_raw">Contigs</a></li>
                <li><a href="#sb3_raw">Annotations</a></li>
            </ul>

            <p>Use the <strong>Upload/Replace</strong> tab to add raw reads. Use the <strong>Actions</strong> tab to download, trim or assemble reads.
            </p>
        </div>        <!-- .sidebar ends -->

        <div class="sidebar_content" id="sb1_raw">
            <h3>Name <span style="font-size: small;">(click to edit)</span></h3>

            <p class="edit_in_place" name="name">${assemblyInstance.name}</p>

            <h3>Statistics</h3>

            <p>
                Contig count : ${assemblyInstance.contigCount}<br/>
                Base count : ${assemblyInstance.baseCount}<br/>
                Min contig length : ${assemblyInstance.minContigLength}<br/>
                Mean contig length : ${assemblyInstance.meanContigLength}<br/>
                Max contig length : ${assemblyInstance.maxContigLength}<br/>
                N50 : ${assemblyInstance.n50}
            </p>
        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb2_raw">
            <p>
                <g:form controller="assembly" action="download" method="get">
                    <g:hiddenField name="id" value="${assemblyInstance.id}"/>
                    <input type="submit" class="submit long" value="Download contigs"/>
                </g:form>
            </p>
        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb3_raw">
            <p>
                <g:form controller="assembly" action="runBlast">
                    <g:hiddenField name="id" value="${assemblyInstance.id}"/>
                    <input type="submit" class="submit long" value="BLAST contigs"/>
                </g:form>
            </p>
            <br/>



            <g:form action="uploadBlastAnnotation" method="post" enctype="multipart/form-data">

                <p class="fileupload"  style="clear:none;">
                    <label>Select BLAST results file to upload:</label><br/>
                    <input type="file" name="myFile"/>

                    <span id="uploadmsg">BLAST XML output only</span>
                </p>

                <g:hiddenField name="id" value="${assemblyInstance?.id}"/>

                <p  style="clear:none;">
                    <input type="submit" class="submit mid" value="Upload"/>
                </p>
            </g:form>

        </div>        <!-- .sidebar_content ends -->

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


<div class="block">
    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Dataset charts</h2>
        <ul class="tabs">
            <li><a href="#tab1">Scatterplot</a></li>
            <li><a href="#tab2">Histogram</a></li>
        </ul>
    </div>        <!-- .block_head ends -->

    <div class="block_content tab_content" id="tab1">

        <h3>Scatterplot</h3>

        <form id="scatterplotForm">
            <input type="hidden" name="id" value="${assemblyInstance.id}"/>

            <p>
                Generate plot of

                <select id="x_label" name="x">
                    <option value="length">Length</option>
                    <option value="quality">Quality</option>
                    <option value="coverage">Coverage</option>
                    <option value="gc">GC</option>
                </select>

                vs

                <select id="y_label" name="y">
                    <option value="length">Length</option>
                    <option value="quality">Quality</option>
                    <option value="coverage">Coverage</option>
                    <option value="gc">GC</option>
                </select>
                <br/>
                Colour contigs by
                <select id="contig_colour" name="colour">
                    <option value="none" selected="true">None</option>
                    <option value="species">Species of top BLAST hit</option>
                    <option value="phylum">Phylum of top BLAST hit</option>
                </select>
                with bitscore cutuff: <input type="text" id="cutoff"/>
                <input type="submit" value="Load plot"/>
                <img style="display: none;" src="${resource(dir: 'images', file: 'spinner.gif')}"
                     id="loadingScatterplotImage"/>
            </p>
        </form>

        <div id='scatterplot'>
            <img id="scatterplotImage"/>
        </div>

    </div>        <!-- .block_content ends -->



    <div class="block_content tab_content" id="tab2">

        <h3>Histogram</h3>

        <form id="histogramForm">
            <p>
                <input type="hidden" name="id" value="${assemblyInstance.id}"/>

                Generate histogram of

                <select id="histogramField" name="field">
                    <option value="length">Length</option>
                    <option value="quality">Quality</option>
                    <option value="coverage">Coverage</option>
                    <option value="gc">GC</option>
                </select>

                scale:

                <select id="histogramScale" name="scale">
                    <option value="lin" selected="true">linear</option>
                    <option value="log">log</option>
                </select>



                <input type="submit" value="Load plot"/>
                <img style="display: none;" src="${resource(dir: 'images', file: 'spinner.gif')}"
                     id="loadingHistogramImage"/>
            </p>
        </form>

        <div id='histogramplot'>
            <img id="histogramImage"/>

        </div>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>

</div>


<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>${assemblyInstance.name}<span style="font-size: 10px;">edit</span></h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <p>${assemblyInstance.description.replaceAll("\n", '<br/>')}</p>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
