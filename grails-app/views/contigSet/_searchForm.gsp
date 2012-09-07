

<form id="contigSetForm" method="get"  class="form-search">

    <input type="hidden" name="idList" value="${contigSetId}">
     <div class="btn-group">
        <button class="btn btn-info" id="showContigSetsButton" type="submit"onclick="submitCompare();"><i class="icon-eye-open"></i>&nbsp;View contig set</button>
        <button class="btn btn-info" id="searchContigSetAnnotationButton" type="submit"><i class="icon-search"></i>&nbsp;Search contigs</button>
        <button class="btn btn-info" id="blastContigSetAnnotationButton" type="submit"><i class="icon-zoom-in"></i>&nbsp;BLAST contigs</button>
        <button class="btn btn-success" id="downloadContigsButton" onClick="submitDownload()"><i class="icon-download-alt"></i>&nbsp;download contigs</button>
    </div>
    <br/><br/>


    <div id="searchForm" style="display:none">

        <div class="input-append">
            <input name="searchQuery" id="searchQuery" type="text" placeholder="Enter search query..." class="search-query input-xlarge">
            <button id="submitSearchButton" type="submit" class="btn" onclick="submitSearchForm();">
            <i class="icon-search"></i>&nbsp;search
            </button>    
        </div>
        <span class="help-block">Hint: use <b>&amp;</b> for AND,  <b>|</b> for OR, <b>(</b> and <b>)</b> to group.</span>

        <label>Results to show:</label>
        <select name="numberOfResults">
            <option value="10">10</option>
            <option value="100">100</option>
            <option value="1000">1000</option>
            <option value="10000">10000</option>
        </select>
        <br/>
        
        <label>Search in libraries (multiple selection):</label><br/>
        <select name="readSource" multiple="true" size="10" class="span8">
        <g:each var="sourceName" in="${readSources}">
            <option value="${sourceName}">${sourceName}</option>
        </g:each>
        </select>                
    </div>
    
    <div id="blastForm" style="display:none">
        <label>BLAST query sequence:</label> <br/>
        <textarea name="blastQuery" id="blastQuery" rows="10" class="span8" placeholder="Paste DNA sequence here..."></textarea>
        <br/><br/>
        <button id="submitBLASTButton" type="submit" class="btn btn-info" onclick="submitBLASTForm();">
            <i class="icon-zoom-in"></i>&nbsp;submit sequence
        </button>
    </div>
</form>

<script type="text/javascript">
    var ua = navigator.userAgent, event = (ua.match(/iPad/i)) ? "touchstart" : "click";
    //    alert('user agent is ' + ua)
    $("#blastContigSetAnnotationButton").bind(event, function(e) {
        console.log('showing blast box');
        showBLASTBox();
        return false;
    });
    $("#showContigSetsButton").bind(event, function(e) {
        submitCompare();
    });
    $("#searchContigSetAnnotationButton").bind(event, function(e) {
        showSearchBox();
        return false;
    });

    document.addEventListener('touch', function(e) {
        e.preventDefault();
        var touch = e.touches[0];
        alert(touch.pageX + " - " + touch.pageY);
    }, false);


</script>

