<table cellpadding="0" cellspacing="0" width="100%" class="sortable" id="contigTable">

    <thead>
    <tr>

        <th width="100px;">Contig ID</th>
        <th width="50px;">Length</th>
        <th width="50px;">Mean quality</th>
        <th width="50px;">Mean coverage</th>
        <th width="50px;">GC</th>
        <th width="50px;">Annotation</th>
        
    </tr>
    </thead>

    <tbody id="contigTableBody">
    <g:each var="contig" in="${contigCollection}" status="index">

        <tr style="display:none;">
            <td><g:link controller="contig" action="show" id="${contig.id}">${contig.name}</g:link></td>
            <td><g:formatNumber number="${contig.length}" type="number" maxFractionDigits="0"  /></td>
            <td><g:formatNumber number="${contig.quality}" type="number" maxFractionDigits="0"  /></td>
            <td><g:formatNumber number="${contig.coverage}" type="number" maxFractionDigits="0"  /></td>
            <td><g:formatNumber number="${contig.gc * 100}" type="number" maxFractionDigits="0"  />%</td>
            <td>
                <g:if test="${contig.topBlast}">
                    BLAST: ${contig.topBlast} (${contig.blastBitscore})<br/>
                </g:if>

                <g:if test="${contig.topPfam}">
                    PFAM: ${contig.topPfam} (${contig.pfamBitscore})
                </g:if>

            </td>
        </tr>
        
    </g:each>
    </tbody>

</table>

<div id="pag"></div>

<script type="text/javascript">
    $(document).ready(function() {

        $('#pag').smartpaginator({
            totalrecords : ${contigCollection.size()},
            recordsperpage:${contigsPerPage},
            datacontainer: 'contigTableBody',
            dataelement: 'tr',
            theme: 'green'
        });

    });
</script>