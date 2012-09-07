<%@ page import="afterparty.ReadsFileStatus; afterparty.Run" %>
<html>
<head>
    <meta name="layout" content="main.gsp"/>
    <title>Run | ${runInstance.name}</title>


    %{--set up edit in place. We will grab all elements with class edit_in_place and run the edit in place method on them.
    To make a bit of text editable we need to
    1. add the edit_in_place tag to it
    2. set the name attribute to be the name of the property that the text refers to --}%
    <script type="text/javascript">
        //         set up edit-in-place
        $(document).ready(function() {
            setUpEditInPlace(
                    ${runInstance.id},
                    "<g:createLink controller="update" action="updateField"/>",
                    'Run'
            );
        });
    </script>

</head>

<body>
<div class="row-fluid">
    <div class="span10 offset1">
        <h2>Run details</h2>
        <h3>Name</h3>
        <p class="edit_in_place" name="name">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${runInstance.name}
        </p>

        <h3>Description</h3>
        <p class="edit_in_place" name="name">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${runInstance.description}
        </p>


        <div class="bs-docs-example">
            <ul id="myTab" class="nav nav-tabs">
               <li class="active"><a href="#rawReads" data-toggle="tab">Raw reads</a></li>
               <li><a href="#trimmedReads" data-toggle="tab">Trimmed reads</a></li>
            </ul>

            <div id="myTabContent" class="tab-content">
                <div class="tab-pane active fade in" id="rawReads">
                    <g:if test="${runInstance.rawReadsFile}">
                        <h3>${runInstance.rawReadsFile.name}</h3>

                        <p>${runInstance.rawReadsFile.description}</p>
                            <g:form controller="readsFile" action="download">
                                <g:hiddenField name="id" value="${runInstance.rawReadsFile.id}"/>
                                <button type="submit" class="btn btn-info"><i class="icon-download-alt"></i>&nbsp;Download reads</button>
                            </g:form>

                        <g:if test="${runInstance.rawReadsFile && isOwner}">
                            <g:form controller="run" action="trim">
                                <g:hiddenField name="id" value="${runInstance.id}"/>
                                <button type="submit" class="btn btn-info"><i class="icon-time"></i>&nbsp;trim reads</button>
                            </g:form>
                        </g:if>

                        <div>
                            <table class="table table-bordered table-hover">
                                <tbody>
                                <tr><td><b>Base count</b> </td><td>${rawReadStats.baseCount} </td></tr>
                                <tr><td><b>Read count</b> </td><td>${rawReadStats.readCount} </td></tr>
                                <tr><td><b>Min length</b> </td><td>${rawReadStats.minLength} </td></tr>
                                <tr><td><b>Mean length</b></td><td> ${rawReadStats.meanLength} </td></tr>
                                <tr><td><b>Max length</b> </td><td>${rawReadStats.maxLength}</td></tr>
                                </tbody>
                            </table>
                        </div>

                    </g:if>
                    <g:else>
                        <h3>No raw reads file uploaded yet</h3>
                    </g:else>

                    <g:if test="${isOwner}">
                        <h4>Upload/replace reads</h4>
                        <g:form action="attachReads" method="post" enctype="multipart/form-data">
                            <label>select file of reads</label>
                            <input type="file" name="myFile"/>
                            <span class="help-block">select a FASTQ file containing raw reads</span>
                            <g:hiddenField name="id" value="${runInstance?.id}"/>
                            <g:hiddenField name="type" value="raw"/>
                            <button type="submit" class="btn btn-info"><i class="icon-upload"></i>&nbsp;upload reads</button>
                        </g:form>
                    </g:if>       
                    <hr/>
                </div>

                <div class="tab-pane fade" id="trimmedReads">
                    <g:if test="${runInstance.trimmedReadsFile}">

                        <h3>${runInstance.trimmedReadsFile.name}</h3>

                        <p><g:truncate maxlength="100">${runInstance.trimmedReadsFile.description}</g:truncate></p>
                        <p>${runInstance.rawReadsFile.description}</p>

                            <g:form controller="readsFile" action="download">
                                <g:hiddenField name="id" value="${runInstance.trimmedReadsFile.id}"/>
                                <button type="submit" class="btn btn-info"><i class="icon-download-alt"></i>&nbsp;Download reads</button>
                            </g:form>

                        <g:if test="${runInstance.trimmedReadsFile && isOwner}">
                            <g:form controller="run" action="runMira">
                                <g:hiddenField name="id" value="${runInstance.id}"/>
                                <button type="submit" class="btn btn-info"><i class="icon-time"></i>&nbsp;assemble reads</button>
                            </g:form>
                        </g:if>

                        <div>
                            <table class="table table-bordered table-hover">
                                <tbody>
                                <tr><td><b>Base count</b> </td><td>${trimmedReadStats.baseCount} </td></tr>
                                <tr><td><b>Read count</b> </td><td>${trimmedReadStats.readCount} </td></tr>
                                <tr><td><b>Min length</b> </td><td>${trimmedReadStats.minLength} </td></tr>
                                <tr><td><b>Mean length</b></td><td> ${trimmedReadStats.meanLength} </td></tr>
                                <tr><td><b>Max length</b> </td><td>${trimmedReadStats.maxLength}</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </g:if>
                    <g:else>
                        <h3>No trimmed reads file uploaded yet</h3>
                    </g:else>

                    <g:if test="${isOwner}">
                        <h4>Upload/replace reads</h4>
                        <g:form action="attachReads" method="post" enctype="multipart/form-data">
                            <label>select file of reads</label>
                            <input type="file" name="myFile"/>
                            <span class="help-block">select a FASTQ file containing raw reads</span>
                            <g:hiddenField name="id" value="${runInstance?.id}"/>
                            <g:hiddenField name="type" value="trimmed"/>
                            <button type="submit" class="btn btn-info"><i class="icon-upload"></i>&nbsp;upload reads</button>
                        </g:form>
                    </g:if>       
                </div>
            </div>
        </div>        
        </div>        <!-- .sidebar_content ends -->

    </div>        <!-- .block_content ends -->
   

</body>
</html>
