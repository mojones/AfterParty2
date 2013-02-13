<%@ page import="afterparty.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <title>Dataset List</title>
</head>

<body>
<g:each in="${studyInstanceList}" status="i" var="studyInstance">
    <div class="row-fluid">
        <div class="span10 offset1 in_a_box study_box">
            <div class="span9">
                <h2><g:link action="show" id="${studyInstance.id}"><i class="icon-th-list"></i>&nbsp;${studyInstance.name}</g:link></h2>
                <p>${studyInstance.description}</p>
                <g:if test="${studyCounts.containsKey(studyInstance.id)}">
                    <p>
                        <b><g:formatNumber number="${studyCounts.get(studyInstance.id).get('contigCount')}" format="###,###,##0" /></b> contigs in <b> <g:formatNumber number="${studyCounts.get(studyInstance.id).get('assembly_count')}" format="###,###,##0" /> </b>assemblies<br/>
                        <b><g:formatNumber number="${studyCounts.get(studyInstance.id).get('BLAST', [:]).get('BLAST', 0)}" format="###,###,##0" /></b> BLAST annotations,
                        <b><g:formatNumber number="${studyCounts.get(studyInstance.id).get('PFAM', [:]).get('PFAM', 0)}" format="###,###,##0" /></b> PFAM annotations,
                        <b><g:formatNumber number="${studyCounts.get(studyInstance.id).get('GENE3D', [:]).get('GENE3D', 0)}" format="###,###,##0" /></b> Gene3D annotations,
                        <b><g:formatNumber number="${studyCounts.get(studyInstance.id).get('COIL', [:]).get('COIL', 0)}" format="###,###,##0" /></b> Coil annotations,
                        <b><g:formatNumber number="${studyCounts.get(studyInstance.id).get('HMMSMART', [:]).get('HMMSMART', 0)}" format="###,###,##0" /></b> HMMSMART annotations,
                        <b><g:formatNumber number="${studyCounts.get(studyInstance.id).get('PHOBIUS', [:]).get('PHOBIUS', 0)}" format="###,###,##0" /></b> PHOBIUS annotations
                </g:if>
<br/>
            </div>
            <div class="span3" style="text-align:center;">
                <img src="${config.imagePath.get(studyInstance.id.toInteger())}" style="max-width:200px;max-height:200px;"/>
            </div>
        </div>        
    </div>        
</g:each>
</body>
</html>
