
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
      <g:each var="contig" in="${contigs}" status="index">

              <td>${contig[0]}</td>
              <td>${contig[1]}</td>
              <td>${contig[2]}</td>
              <td>${contig[3]}</td>
              <td>${contig[4]}</td>
              <td>${contig[5]}</td>
          </tr>
          
      </g:each>        
    </tbody>
</table>

<script type="text/javascript">
$(document).ready(function() {
   $('#contigTable').dataTable({
        "aaSorting": [[ 3, "desc" ]],
        "asStripeClasses": [],
        "sPaginationType": "bootstrap"
   });
});
</script>

