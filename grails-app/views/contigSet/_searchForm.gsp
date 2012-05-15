<form id="contigSetForm" method="get">

    <input type="hidden" name="idList" value="${contigSetId}">
    <input class="submit long" id="showContigSetsButton" type="submit" value="view contigs" onclick="submitCompare();"/>

    <input class="submit long" id="searchContigSetAnnotationButton" onclick="showSearchBox();
    return false;" type="submit" class="submit long" value="search contigs">
    <input class="submit long" id="blastContigSetAnnotationButton" onclick="showBLASTBox();
    return false;" type="submit" class="submit long" value="blast contigs">

    <br/>

    <p id="blastForm" style="display:none" class="doSomethingButton">
        <label>BLAST query sequence:</label> <br/><br/>
        <textarea name="blastQuery" id="blastQuery" rows="40" cols="80"></textarea>
        <br/><br/>
        <input id="submitBLASTButton" type="submit" class="submit long" value="submit" onclick="submitBLASTForm();">
    </p>

    <p id="searchForm" style="display:none" class="doSomethingButton">
        <label>Search query:</label> <br/><br/>
        <input name="searchQuery" id="searchQuery" type="text" class="text small"> <br/><br/>    <label>Results to show:</label>
        <select name="numberOfResults">
            <option value="10">10</option>
            <option value="100">100</option>
            <option value="1000">1000</option>
            <option value="10000">10000</option>
        </select>
        <br/><br/>
        Hint: use <b>&amp;</b> for AND,  <b>|</b> for OR, <b>(</b> and <b>)</b> to group.
        <br/><br/>
        <input id="submitSearchButton" type="submit" class="submit long" value="submit" onclick="submitSearchForm();">
    </p>
</form>
