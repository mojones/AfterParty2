
<table class="table table-bordered table-hover" id="contigTable">
    <thead>
        <tr>
            <th>Contig name</th>
            <th>Length</th>
            <th>Mean quality</th>
            <th>Mean coverage</th>
            <th>GC</th>
            <th>Annotation</th>
        
        </tr>
    </thead>

    <tbody id="contigTableBody">
        
    </tbody>
</table>

<script type="text/javascript">
$(document).ready(function() {
   $('#contigTable').dataTable({
        "aaSorting": [[ 3, "desc" ]],
        "asStripeClasses": [],
        "sPaginationType": "bootstrap",
        "bServerSide" : true,
        "bProcessing": true,
        "sAjaxSource" : "${createLink(controller:'contigSet', action:'getContigsJSON', params :[contigSetId : contigSetId])}",
        "aoColumns": [
          null,
          null,
          null,
          null,
          { "bSortable": false },
          { "bSortable": false }
        ]
   });
});
</script>