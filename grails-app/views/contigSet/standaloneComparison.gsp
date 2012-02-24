<%@ page import="afterparty.Assembly" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'assembly.label', default: 'Assembly')}"/>
    <title>Standalone contig comparion page</title>



    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
</head>

<body>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Upload files</h2>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

    <!-- .sidebar ends -->


        <g:form action="uploadContigsStandalone" method="post" enctype="multipart/form-data">
            <h2>Upload assemblies</h2>

            <p class="fileupload" style="clear:none;">
                <label>Assembly 1 upload:</label><br/>
                <input type="file" name="fasta_1"/>
                <span id="uploadmsg">FASTA format only</span>
            </p>

            <p class="fileupload" style="clear:none;">
                <label>Assembly 2 upload:</label><br/>
                <input type="file" name="fasta_2"/>
                <span id="uploadmsg">FASTA format only</span>
            </p>

            <p class="fileupload" style="clear:none;">
                <label>Assembly 3 upload:</label><br/>
                <input type="file" name="fasta_3"/>
                <span id="uploadmsg">FASTA format only</span>
            </p>

            <p class="fileupload" style="clear:none;">
                <label>Assembly 4 upload:</label><br/>
                <input type="file" name="fasta_4"/>
                <span id="uploadmsg">FASTA format only</span>
            </p>

            <p class="fileupload" style="clear:none;">
                <label>Assembly 5 upload:</label><br/>
                <input type="file" name="fasta_5"/>
                <span id="uploadmsg">FASTA format only</span>
            </p>

            <p class="fileupload" style="clear:none;">
                <label>Assembly 6 upload:</label><br/>
                <input type="file" name="fasta_6"/>
                <span id="uploadmsg">FASTA format only</span>
            </p>

            <p class="fileupload" style="clear:none;">
                <label>Assembly 7 upload:</label><br/>
                <input type="file" name="fasta_7"/>
                <span id="uploadmsg">FASTA format only</span>
            </p>

            <p class="fileupload" style="clear:none;">
                <label>Assembly 8 upload:</label><br/>
                <input type="file" name="fasta_8"/>
                <span id="uploadmsg">FASTA format only</span>
            </p>

            <p style="clear:none;">
                <input type="submit" class="submit short" value="Upload"/>
            </p>
        </g:form>


    <!-- .sidebar_content ends -->

    </div>        <!-- .block_content ends -->
    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
