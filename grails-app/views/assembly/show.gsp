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

            <p>Use the <strong>Contigs</strong> tab to download/upload contigs. Use the <strong>Annotations</strong> tab to run BLAST or upload a BLAST result.
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

            <br/><br/>

            <h2>Upload contigs</h2>
            <g:form action="uploadContigs" method="post" enctype="multipart/form-data">

                <p class="fileupload" style="clear:none;">
                    <label>Contigs file to upload:</label><br/>
                    <input type="file" name="myFile"/>
                    <span id="uploadmsg">FASTA format only</span>
                </p>

                <p class="fileupload" style="clear:none;">
                    <label>Contigs quality file (optional):</label><br/>
                    <input type="file" name="contigsQualityFile"/>
                    <span id="uploadmsg">FASTA quality format only</span>
                </p>

                <p class="fileupload" style="clear:none;">
                    <label>Contigs statistics file (optional):</label><br/>
                    <input type="file" name="contigsStatsFile"/>
                    <span id="uploadmsg">MIRA output  format only</span>
                </p>

                <g:hiddenField name="id" value="${assemblyInstance?.id}"/>

                <p style="clear:none;">
                    <input type="submit" class="submit short" value="Upload"/>
                </p>
            </g:form>

            <h2>Upload ACE</h2>
            <g:form action="uploadAce" method="post" enctype="multipart/form-data">
                <p class="fileupload" style="clear:none;">
                    <label>.ace file to upload:</label><br/>
                    <input type="file" name="aceFile"/>
                    <span id="uploadmsg">ACE format only</span>
                </p>
                <g:hiddenField name="id" value="${assemblyInstance?.id}"/>

                <p style="clear:none;">
                    <input type="submit" class="submit short" value="Upload"/>
                </p>
            </g:form>


            <g:if test="${assemblyInstance.defaultContigSet}">
                <g:form controller="contigSet" action="compareContigSets" method="get">

                    <g:hiddenField name="idList" value="${assemblyInstance.defaultContigSet.id}"/>

                    <p style="clear:none;">
                        <input type="submit" class="submit mid" value="View contigs"/>
                    </p>
                </g:form>
            </g:if>
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

                <p class="fileupload" style="clear:none;">
                    <label>Select BLAST results file to upload:</label><br/>
                    <input type="file" name="myFile"/>

                    <span id="uploadmsg">BLAST XML output only</span>
                </p>

                <g:hiddenField name="id" value="${assemblyInstance?.id}"/>

                <p style="clear:none;">
                    <input type="submit" class="submit mid" value="Upload"/>
                </p>
            </g:form>

        </div>        <!-- .sidebar_content ends -->

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>




<g:if test="${contigs.size() > 10}">

    <div class="block">
        <div class="block_head">
            <div class="bheadl"></div>

            <div class="bheadr"></div>

            <h2>Browse contigs for this assembly</h2>
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
                <g:each var="contig" in="${contigs[0..10]}" status="index">

                    <tr>
                        <td><g:link controller="contig" action="show" id="${contig.id}">${contig.name}</g:link></td>
                        <td>${contig.length()}</td>
                        <td>${contig.reads.size()}</td>

                    </tr>
                </g:each>
                </tbody>

            </table>

            <g:set var="totalPages" value="${assemblyInstance.contigs.size() / 10}"/>
            <g:if test="${totalPages == 1}"><span class="currentStep">1</span></g:if>
            <g:else>

                <div class="pagination left">
                    <g:paginate controller="assembly" action="show" params="[id: assemblyInstance.id]"
                                total="${assemblyInstance.contigs.size()}"
                                prev="&lt; previous" next="next &gt;"/>
                </div>        <!-- .pagination ends -->

            </g:else>
        </div>        <!-- .block_content ends -->
        <div class="bendl"></div>

        <div class="bendr"></div>
    </div>
</g:if>


<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>${assemblyInstance.name}</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <p>${assemblyInstance.description.replaceAll("\n", '<br/>')}</p>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
