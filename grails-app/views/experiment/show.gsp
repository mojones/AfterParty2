<%@ page import="afterparty.Experiment" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'experiment.label', default: 'Experiment')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>


    %{--set up edit in place. We will grab all elements with class edit_in_place and run the edit in place method on them.
    To make a bit of text editable we need to
    1. add the edit_in_place tag to it
    2. set the name attribute to be the name of the property that the text refers to --}%
    <script type="text/javascript">
        //         set up edit-in-place
        $(document).ready(function() {
            setUpEditInPlace(
                    ${experimentInstance.id},
                    "<g:createLink controller="update" action="updateField"/>",
                    'Experiment'
            );
        });
    </script>

</head>

<body>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>${experimentInstance.name}<span style="font-size: 10px;">edit</span></h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <p>${experimentInstance.description}</p>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Runs</h2>

        <ul>
            <li><a href="#">Add new</a></li>
            <li><a href="#">Add page</a></li>
        </ul>
    </div>        <!-- .block_head ends -->

    <div class="block_content">

        <table cellpadding="0" cellspacing="0" width="100%" class="sortable">
            <thead>
            <tr>
                <th>Run name</th>
                <th>Raw reads</th>
                <td>Trimmed reads</td>
            </tr>
            </thead>
            <tbody>
            <g:each in="${experimentInstance.runs}" var="s">
                <tr>
                    <td><g:link controller="run" action="show" id="${s.id}">${s.name}</g:link></td>
                    <td>${s.getRawReadsFile().readCount}</td>
                    <td>1234567</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Upload / Replace adapter sequences</h2>

    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <g:form action="attachAdapterSequences" method="post" enctype="multipart/form-data">

            <p class="fileupload">
                <label>Select new file:</label><br/>
                <input type="file" name="myFile"/>

                <span id="uploadmsg">FASTA format only</span>
            </p>

            <g:hiddenField name="experimentId" value="${experimentInstance?.id}"/>

            <p>
                <input type="submit" class="submit mid" value="Upload"/>
            </p>
        </g:form>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>

</div>



<h3><g:link controller="experiment" action="trimAllReadFiles" id="${experimentInstance.id}">Trim all reads</g:link></h3>

</body>
</html>
