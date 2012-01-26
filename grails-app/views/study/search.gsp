<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.springframework.util.ClassUtils" %>
<%@ page import="grails.plugin.searchable.internal.lucene.LuceneUtils" %>
<%@ page import="grails.plugin.searchable.internal.util.StringQueryUtils" %>

<html>
<head>
    <meta name="layout" content="main"/>

    <title>Search contigs</title>

</head>

<body>
<div>

    <div class="block">

        <div class="block_head">
            <div class="bheadl"></div>

            <div class="bheadr"></div>

            <h2>Search contigs</h2>

        </div>        <!-- .block_head ends -->



        <div class="block_content">

            <g:form url='[controller: "study", action: "search"]' id="searchableForm" name="searchableForm"
                    method="get">
                <g:hiddenField name="id" value="${studyInstance.id}"/>
                <p>
                    <label>Search string:</label><br/>
                    <g:textField name="q" value="${params.q}" class="text small"/>
                    <span class="note">*boolean operators allowed</span>
                </p>


                <p><label>Search assembly:</label> <br/>



                    <g:each in="${assemblies}" var="a">
                        <g:checkBox name="check_${a.id}" value="${true}"/>
                        ${a.name}
                        <br/>
                    </g:each>


                <p>
                    <input type="submit" class="submit small" value="Search"/>
                </p>

                <h2>Search tips</h2>

                <p>
                    Simple search : "<strong>ribosomal</strong>"                                   <br/>
                </p>

                <h2>Searching BLAST annotation</h2>

                <p>
                    Search for BLAST hit accession : "<strong>accession:Q9CKG4</strong>"             <br/>
                    Search for BLAST hit bitscore in a range: "<strong>bitscore:[100 TO 200]</strong>" or open-ended "<strong>bitscore:[80 TO *]</strong>"<br/>
                </p>

                <h2>Searching contig stats</h2>

                <p>
                    Search for DNA sequence in a contig directly : "<strong>sequence:ATGCTT</strong>" or fuzzily with edit distance : "<strong>sequence:ATGCTT~0.8</strong>"<br/>
                    Search for contigs with a read count in a range : "<strong>readCount:[3 TO 5]</strong>", open-ended : "<strong>readCount:[9 TO *]</strong>" or exact : : "<strong>readCount:22</strong>"<br/>
                    Search for contigs by length : "<strong>length:[1500 TO 1600]</strong>"<br/>
                    Search for contigs by average quality : "<strong>averageQuality:[80 TO *]</strong>"<br/>
                    Search for contigs by average coverage : "<strong>averageCoverage:[6 TO *]</strong>"<br/>
                    Search for contigs by GC content : "<strong>gc[50 TO 60]</strong>"<br/>
                </p>

                <h2>Combine search criteria</h2>

                <p>
                    1000 - 2000 bp length and annotated as ribosomal : "<strong>ribosomal AND length:[1000 TO 2000]</strong>"<br/>
                    kinases and oxidases with at least 3 reads and GC between 40% and 50% : "<strong>(kinase OR oxidase) AND gc:[40 TO 50] AND readCount:[3 TO *]</strong>"<br/>
                </p>

            </g:form>

        </div>        <!-- .block_content ends -->

        <div class="bendl"></div>

        <div class="bendr"></div>

    </div>


    <g:set var="haveQuery" value="${params.q?.trim()}"/>
    <g:set var="haveResults" value="${searchResult?.results}"/>

    <g:if test="${haveResults}">

        <div class="block">
            <div class="block_head">
                <div class="bheadl"></div>

                <div class="bheadr"></div>

                <h2>
                    Showing ${searchResult.offset + 1} - ${searchResult.results.size() + searchResult.offset} of ${searchResult.total} results for ${params.q}

                </h2>
            </div>        <!-- .block_head ends -->

            <div class="block_content">
                <table cellpadding="0" cellspacing="0" width="100%" class="sortable">

                    <thead>
                    <tr>

                        <th>Contig ID</th>
                        <th>Top Hit description</th>
                        <th>Top Hit bitscore</th>
                    </tr>
                    </thead>

                    <tbody>
                    <g:each var="result" in="${searchResult.results}" status="index">

                        <tr>
                            <td><g:link controller="contig" action="show" id="${result.id}">${result.name}</g:link></td>
                            <td>${result.topBlastHitMatching(params.q).description}</td>
                            <td>${result.topBlastHitMatching(params.q).bitscore}</td>

                        </tr>
                    </g:each>
                    </tbody>

                </table>

                <g:set var="totalPages" value="${Math.ceil(searchResult.total / searchResult.max)}"/>
                <g:if test="${totalPages == 1}"><span class="currentStep">1</span></g:if>
                <g:else>

                    <div class="pagination left">
                        <g:paginate controller="contig" action="search" params="[q: params.q]"
                                    total="${searchResult.total}"
                                    prev="&lt; previous" next="next &gt;"/>
                    </div>        <!-- .pagination ends -->

                </g:else>
            </div>        <!-- .block_content ends -->
            <div class="bendl"></div>

            <div class="bendr"></div>
        </div>

    </g:if>

</body>
</html>