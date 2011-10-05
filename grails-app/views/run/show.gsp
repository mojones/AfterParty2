<%@ page import="afterparty.ReadsFileStatus; afterparty.Run" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'run.label', default: 'Run')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>


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

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>${runInstance.name}<span style="font-size: 10px;">edit</span></h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <p>${runInstance.description}</p>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>


<div class="block withsidebar small left">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Raw reads</h2>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <div class="sidebar">
            <ul class="sidemenu">
                <li><a href="#sb1_raw">Description</a></li>
                <li><a href="#sb2_raw">Upload/Replace</a></li>
                <li><a href="#sb3_raw">Actions</a></li>
            </ul>

            <p><strong>Some notification text.</strong> Donec hendrerit porttitor felis, id semper eros lobortis sed. Class aptent taciti sociosqu ad litora.
            </p>
        </div>        <!-- .sidebar ends -->

        <div class="sidebar_content" id="sb1_raw">
            <h3>${runInstance.rawReadsFile.name}</h3>

            <p>${runInstance.rawReadsFile.description}</p>

        <p>
                <b>Base count</b> : ${runInstance.rawReadsFile.baseCount} <br/>
                <b>Read count</b> : ${runInstance.rawReadsFile.readCount} <br/>
                <b>Min length</b> : ${runInstance.rawReadsFile.minReadLength} <br/>
                <b>Mean length</b> : ${runInstance.rawReadsFile.meanReadLength} <br/>
                <b>Max length</b> : ${runInstance.rawReadsFile.maxReadLength}
            </p>

        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb2_raw">
            <g:form action="attachRawReads" method="post" enctype="multipart/form-data">
                <p class="fileupload" style="clear:none;">
                    <label>Select new file:</label><br/>
                    <input type="file" name="myFile"/>
                </p>
                <g:hiddenField name="runId" value="${runInstance?.id}"/>
                <p style="clear:none;">
                    <input type="submit" class="submit long" value="Upload new file"/>
                </p>
            </g:form>
        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb3_raw">
            <p>
                <g:form controller="readsFile" action="download">
                    <g:hiddenField name="id" value="${runInstance.rawReadsFile.id}"/>
                    <input type="submit" class="submit long" value="Download reads"/>
                </g:form>
            </p>

            <p>
                <g:form controller="readsFile" action="trim">
                    <g:hiddenField name="id" value="${runInstance.rawReadsFile.id}"/>
                    <input type="submit" class="submit long" value="Trim reads"/>
                </g:form>
            </p>

            <p>
                <g:form controller="readsFile" action="runMira">
                    <g:hiddenField name="id" value="${runInstance.rawReadsFile.id}"/>
                    <input type="submit" class="submit long" value="Assemble reads"/>
                </g:form>
            </p>

        </div>        <!-- .sidebar_content ends -->

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block withsidebar small right">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Trimmed reads</h2>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <div class="sidebar">
            <ul class="sidemenu">
                <li><a href="#sb1_trimmed">Description</a></li>
                <li><a href="#sb2_trimmed">Upload/Replace</a></li>
                <li><a href="#sb3_trimmed">Actions</a></li>
            </ul>

            <p><strong>Some notification text.</strong> Donec hendrerit porttitor felis, id semper eros lobortis sed. Class aptent taciti sociosqu ad litora.
            </p>
        </div>        <!-- .sidebar ends -->

        <div class="sidebar_content" id="sb1_trimmed">
            <h3>${runInstance.trimmedReadsFile.name}</h3>

            <p><g:truncate maxlength="100">${runInstance.trimmedReadsFile.description}</g:truncate></p>

            <p>
                <b>Base count</b> : ${runInstance.trimmedReadsFile.baseCount} <br/>
                <b>Read count</b> : ${runInstance.trimmedReadsFile.readCount} <br/>
                <b>Min length</b> : ${runInstance.trimmedReadsFile.minReadLength} <br/>
                <b>Mean length</b> : ${runInstance.trimmedReadsFile.meanReadLength} <br/>
                <b>Max length</b> : ${runInstance.trimmedReadsFile.maxReadLength}
            </p>
        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb2_trimmed">
            <g:form action="attachTrimmedReads" method="post" enctype="multipart/form-data">
                <p class="fileupload" style="clear:none;">
                    <label>Select new file:</label><br/>
                    <input type="file" name="myFile"/>
                </p>
                <g:hiddenField name="runId" value="${runInstance?.id}"/>
                <p style="clear:none;">
                    <input type="submit" class="submit long" value="Upload new file"/>
                </p>
            </g:form>
        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb3_trimmed">
            <p>
                <g:form controller="readsFile" action="download">
                    <g:hiddenField name="id" value="${runInstance.trimmedReadsFile.id}"/>
                    <input type="submit" class="submit long" value="Download reads"/>
                </g:form>
            </p>

            <p>
                <g:form controller="readsFile" action="runMira">
                    <g:hiddenField name="id" value="${runInstance.trimmedReadsFile.id}"/>
                    <input type="submit" class="submit long" value="Assemble reads"/>
                </g:form>
            </p>
        </div>        <!-- .sidebar_content ends -->

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
