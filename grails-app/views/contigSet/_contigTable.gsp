<table cellpadding="0" cellspacing="0" width="100%" class="sortable" id="contigTable">

    <thead>
    <tr>

        <th width="100px;">Contig ID</th>
        <th width="50px;">Length</th>
        <th width="50px;">Coverage</th>
        <th width="50px;">Quality</th>
        <th width="50px;">GC</th>
    </tr>
    </thead>

    <tbody id="contigTableBody">
    <g:each var="contig" in="${contigCollection}" status="index">

        <tr style="display:none;">
            <td><g:link controller="contig" action="show" id="${contig.id}">${contig.name}</g:link></td>
            <td>${contig.length}</td>
            <td>${contig.averageCoverage}</td>
            <td>${contig.averageQuality}</td>
            <td>${contig.gc()}</td>
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