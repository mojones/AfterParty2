<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.springframework.util.ClassUtils" %>
<%@ page import="grails.plugin.searchable.internal.lucene.LuceneUtils" %>
<%@ page import="grails.plugin.searchable.internal.util.StringQueryUtils" %>

<html>
<head>
    <meta name="layout" content="main.gsp"/>

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

            <g:form url='[action: "search"]' id="searchableForm" name="searchableForm" method="get">
                <p>
                    <label>Search string:</label><br/>
                    <g:textField name="q" value="${params.q}" class="text small"/>
                    <span class="note">*boolean operators allowed</span>
                </p>


                <p><label>Search assembly:</label> <br/>

                    <select class="styled" name="assemblyId">
                        <g:each in="${assemblies}" var="a">
                            <option value="${a.id}">${a.name}</option>
                        </g:each>
                    </select></p>


                <p>
                    <input type="submit" class="submit small" value="Search"/>
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