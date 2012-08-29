<p>
    Annotation key: <i class="icon-zoom-in"></i>&nbsp; BLAST vs UniProt&nbsp;&nbsp;&nbsp;<i class="icon-book"></i>&nbsp;PFAM 
</p>
<table class="table table-bordered table-hover" id="contigTable">
    <thead>
        <tr>
            <th>Contig ID</th>
            <th>Length</th>
            <th>Mean quality</th>
            <th>Mean coverage</th>
            <th>GC</th>
            <th>Annotation</th>
        
        </tr>
    </thead>

    <tbody id="contigTableBody">
        <g:each var="contig" in="${contigCollection}" status="index">
            <g:link controller="contig" action="show" id="${contig.id}">
            <tr>
                <td>${contig.name}</td>
                <td><g:formatNumber number="${contig.length}" type="number" maxFractionDigits="0"  /></td>
                <td><g:formatNumber number="${contig.quality}" type="number" maxFractionDigits="0"  /></td>
                <td><g:formatNumber number="${contig.coverage}" type="number" maxFractionDigits="0"  /></td>
                <td><g:formatNumber number="${contig.gc * 100}" type="number" maxFractionDigits="0"  />%</td>
                <td>
                    <g:if test="${contig.topBlast}">
                        <i class="icon-zoom-in"></i>&nbsp;${contig.topBlast} (${contig.blastBitscore})<br/>
                    </g:if>

                    <g:if test="${contig.topPfam}">
                        <i class="icon-book"></i>&nbsp;${contig.topPfam} (${contig.pfamBitscore})
                    </g:if>

                </td>
            </tr>
            </g:link>
        </g:each>
    </tbody>
</table>

<script type="text/javascript">
$(document).ready(function() {
   $('#contigTable').dataTable({
        "aaSorting": [[ 3, "desc" ]],
        "asStripeClasses": [ 'strip1', 'strip2', 'strip3' ]    
   });
});
</script>