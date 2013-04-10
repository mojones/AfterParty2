
<table class="table table-bordered table-hover" id="contigTable">
    <thead>
        <tr>
            <th>Contig name</th>
            <th>Length</th>
            <th>Mean quality</th>
            <th>Mean coverage</th>
            <th>GC</th>
            <th style="width:50%;">Annotation</th>
        
        </tr>
    </thead>

    <tbody id="contigTableBody">
        
    </tbody>
</table>

<script type="text/javascript">
$(document).ready(function() {
   window.contig_table = $('#contigTable').dataTable({
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
   $('.dataTables_filter input').attr("placeholder", "enter seach terms here");
   $('.dataTables_filter input').after('<button type="submit" class="btn" style="-webkit-border-radius: 0 14px 14px 0; vertical-align:top"><i class="icon-search"></i>&nbsp;search</button>')
   $('.dataTables_filter input')
        .unbind('keypress keyup')
        .bind('keypress keyup', function(e){
          if (e.keyCode != 13) return;
          window.contig_table.fnFilter($(this).val());
    });
});
</script>
