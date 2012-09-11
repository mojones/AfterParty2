<%@ page import="afterparty.Assembly" %>
<html>
<head>
    <meta name="layout" content="main.gsp"/>

    <title>Assembly | ${assemblyInstance.name}</title>


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
<div class="row-fluid">

    <div class="span11 offset1">
        <h2>Assembly details</h2>
    </div>
</div>

<div class="row-fluid">

    <div class="span4 offset1">
        <h3>Name</h3>
        <p class="edit_in_place" name="name">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${assemblyInstance.name}
        </p>

        <h3>Description</h3>

        <p class="edit_in_place" name="description">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${assemblyInstance.description.replaceAll("\n", '<br/>')}
        </p>


    </div>
    <div class="span2">
        <g:if test="${assemblyInstance.defaultContigSet}">

        <table class="table table-bordered table-hover">
                <tbody>
                    <tr> <td> <b>Contig count</b> </td> <td> ${stats.count} </td> </tr>
                    <tr> <td> <b>Base count</b> </td> <td> ${stats.span}</td> </tr>
                    <tr> <td> <b>Min contig length</b> </td> <td> ${stats.min}</td> </tr>
                    <tr> <td> <b>Mean contig length</b> </td> <td> ${stats.mean}</td> </tr>
                    <tr> <td> <b>Max contig length</b> </td> <td> ${stats.max}</td> </tr>
                </tbody>
            </table>
        </g:if>
        
    </div>
<div>

<div class="row-fluid">
    <div class="span10 offset1">
        <g:if test="${assemblyInstance.defaultContigSet}">
            <g:render template="/contigSet/searchForm" model="['contigSetId' : assemblyInstance.defaultContigSet.id, 'readSources' : readSources]"/>
        </g:if>


        <div class="bs-docs-example">
            <ul id="myTab" class="nav nav-tabs">
              <li class="active"><a href="#generateAnnotation" data-toggle="tab">Generate annotation</a></li>
              
              <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Upload contigs <b class="caret"></b></a>
                <ul class="dropdown-menu">
                  <li><a href="#uploadFASTA" data-toggle="tab">FASTA</a></li>
                  <li><a href="#uploadACE" data-toggle="tab">ACE</a></li>
                </ul>
              </li>

              <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Upload annotation <b class="caret"></b></a>
                <ul class="dropdown-menu">
                  <li><a href="#uploadBLAST" data-toggle="tab">BLAST</a></li>
                  <li><a href="#uploadIPR" data-toggle="tab">InterProScan</a></li>
                </ul>
              </li>
            </ul>

            <div id="myTabContent" class="tab-content">
                <div class="tab-pane active fade in" id="generateAnnotation">
                    <g:form controller="assembly" action="runBlast">
                        <g:hiddenField name="id" value="${assemblyInstance.id}"/>
                        <button type="submit" class="btn btn-info"/><i class="icon-time"></i>&nbsp;generate BLAST annotation</button>
                    </g:form>

                    <g:form controller="assembly" action="runPfam">
                        <g:hiddenField name="id" value="${assemblyInstance.id}"/>
                        <button type="submit" class="btn btn-info"/><i class="icon-time"></i>&nbsp;generate InterProScan annotation</button>
                    </g:form>
                </div>

                <div class="tab-pane fade" id="uploadFASTA">
                    <g:form action="uploadContigs" method="post" enctype="multipart/form-data">

                        <label>Contigs file to upload:</label>
                        <input type="file" name="myFile"/>
                        <span class="help-block">FASTA format only</span>
                    
                        <label>Contigs quality file (optional):</label>
                        <input type="file" name="contigsQualityFile"/>
                        <span class="help-block">FASTA quality format only</span>
                    
                        <label>Contigs statistics file (optional):</label>
                        <input type="file" name="contigsStatsFile"/>
                        <span class="help-block">MIRA format only</span>
                    
                        <g:hiddenField name="id" value="${assemblyInstance?.id}"/>

                        <button type="submit" class="btn btn-info"/><i class="icon-time"></i>&nbsp;Upload and create contigs</button>
                    </g:form>
                </div>

                <div class="tab-pane fade" id="uploadACE">
                    <g:form action="uploadAce" method="post" enctype="multipart/form-data">
                        <label>.ace file to upload:</label>
                        <input type="file" name="aceFile"/>
                        <span class="help-block">.ace format only</span>
                        <g:hiddenField name="id" value="${assemblyInstance?.id}"/>
                        <button type="submit" class="btn btn-info"/><i class="icon-time"></i>&nbsp;Upload and create contigs</button>
                    </g:form>                
                </div>

                <div class="tab-pane fade" id="uploadBLAST">
                    <g:form action="uploadBlastAnnotation" method="post" enctype="multipart/form-data">
                        <label>Select BLAST results file to upload:</label>
                        <input type="file" name="myFile"/>
                        <span class="help-block">gzipped BLAST XML format only : <code>-outfmt 5</code></span>
                        <g:hiddenField name="id" value="${assemblyInstance?.id}"/>
                        <button type="submit" class="btn btn-info"/><i class="icon-time"></i>&nbsp;Upload and add annotation</button>
                    </g:form>              
                </div>

                <div class="tab-pane fade" id="uploadIPR">
                    <g:form action="uploadInterproscanAnnotation" method="post" enctype="multipart/form-data">
                        <label>Select InterProScan results file to upload:</label>
                        <input type="file" name="myFile"/>
                        <span class="help-block">InterProScan raw format only <code>-format raw</code></span>
                        <g:hiddenField name="id" value="${assemblyInstance?.id}"/>
                        <button type="submit" class="btn btn-info"/><i class="icon-time"></i>&nbsp;Upload and add annotation</button>
                    </g:form>              
                </div>
            </div>
        </div>

        <g:if test="${assemblyInstance.defaultContigSet}">
            <h2>Browse contigs for this assembly</h2>
            <g:render template="/contigSet/contigTable" model="[contigSetId : assemblyInstance.defaultContigSet.id]"/>
        </g:if>


    </div>
</div>

</body>
</html>
