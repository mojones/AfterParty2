<form id="contigSetForm" method="get"  class="form-search">

    <input type="hidden" name="idList" value="${contigSetId}">
     <div class="btn-group">
        <button class="btn btn-info btn-large" id="showContigSetsButton" type="submit"onclick="submitCompare();"><i class="icon-list"></i>&nbsp;View contig set</button>
        <button class="btn btn-info btn-large" id="searchContigSetAnnotationButton" type="submit"><i class="icon-search"></i>&nbsp;Search contigs</button>
        <button class="btn btn-info btn-large" id="blastContigSetAnnotationButton" type="submit"><i class="icon-align-left"></i>&nbsp;BLAST contigs</button>
    </div>
    <br/><br/>


    <div id="searchForm" style="display:none">

        <div class="input-append">
            <input name="searchQuery" id="searchQuery" type="text" placeholder="Enter search query..." class="search-query input-xlarge">
            <button id="submitSearchButton" type="submit" class="btn" onclick="submitSearchForm();">Search</button>    
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
        <select name="readSource" multiple="true" size="10">
        <g:each var="sourceName" in="${readSources}">
            <option value="${sourceName}">${sourceName}</option>
        </g:each>
        </select>                
    </div>
    
    <div id="blastForm" style="display:none">
        <label>BLAST query sequence:</label> <br/>
        <textarea name="blastQuery" id="blastQuery" rows="10" class="span8" placeholder="Paste DNA sequence here..."></textarea>
        <br/><br/>
        <input id="submitBLASTButton" type="submit" class="btn btn-large btn-info" value="submit" onclick="submitBLASTForm();">
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

