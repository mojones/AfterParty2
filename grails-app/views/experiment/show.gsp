<%@ page import="afterparty.Experiment" %>
<html>
<head>
    <meta name="layout" content="main.gsp"/>
    <title>Experiment | ${experimentInstance.name}</title>


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
<div class="row-fluid">
    <div class="span10 offset1">
        <h2>Experiment details</h2>
        <h3>Name</h3>
        <p class="edit_in_place" name="name">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${experimentInstance.name}
        </p>

        <h3>Description</h3>
        <p class="edit_in_place" name="name">
            <g:if test="${isOwner}">
                <i class="icon-pencil"></i>&nbsp;
            </g:if>
            ${experimentInstance.description}
        </p>

        <h2>Runs</h2>

        <g:if test="${isOwner}">
            <p>
                <g:link class="btn btn-info" controller="experiment" action="createRun" params="${[id : experimentInstance.id]}">
                    <i class="icon-plus-sign"></i>&nbsp; Add new run
                </g:link>
            </p>
        </g:if>

        <g:if test="${experimentInstance.runs}">

            <table class="table table-bordered table-hover">
                <thead>
                <tr>
                    <th>Run name</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${experimentInstance.runs}" var="run">
                    <tr>
                        <td><g:link controller="run" action="show" id="${run.id}"><i class="icon-cog"></i>&nbsp;${run.name}</g:link></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>
        <g:else>
            <h3>Click "ADD NEW" to add an run for this experiment.</h3>
        </g:else>

        <g:if test="${isOwner}">
            <h2>Upload / Replace adapter sequences</h2>
            <g:form action="attachAdapterSequences" method="post" enctype="multipart/form-data">

                <label>Select new file:</label><br/>
                <input type="file" name="myFile"/>
                <span class="help-block">FASTA format only</span>
                <g:hiddenField name="id" value="${experimentInstance?.id}"/>
                <button type="submit" class="btn btn-info"/><i class="icon-upload"></i>&nbsp;upload</button>
            </g:form>
                <g:link class="btn btn-info" controller="experiment" action="trimAllReadFiles" id="${experimentInstance.id}"><i class="icon-time"></i>&nbsp;trim all reads</g:link>


        </g:if>

    </div>    
</div>


</body>
</html>
