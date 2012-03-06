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

    <g:if test="${contigs.size() > 0}">

        <div class="block">
            <div class="block_head">
                <div class="bheadl"></div>

                <div class="bheadr"></div>

                <h2>
                    Showing results ${offset} to ${max} of ${contigs.size()} results for ${query}
                </h2>

            </div>        <!-- .block_head ends -->

            <div class="block_content">

                <table cellpadding="0" cellspacing="0" width="100%" class="sortable">

                    <thead>
                    <tr>

                        <th>Result</th>
                        <th>Contig ID</th>
                        <th>Assembly</th>
                        <th>Top Hit description</th>
                        <th>Top Hit bitscore</th>
                    </tr>
                    </thead>

                    <tbody>
                    <g:each var="contig" in="${contigs[offset..max]}" status="index">
                    %{--comment--}%
                        <tr>
                            <td>${index + offset}</td>
                            <td><g:link controller="contig" action="show" id="${contig.id}">${contig.name}</g:link></td>
                            <td>${contig.assembly.name}</td>
                            <td>${contig.topBlastHitMatching(params.q).description}</td>
                            <td>${contig.topBlastHitMatching(params.q).bitscore}</td>

                        </tr>
                    </g:each>
                    </tbody>

                </table>

                <g:set var="totalPages" value="${Math.ceil(contigs.size() / 100)}"/>
                <g:if test="${totalPages == 1}"><span class="currentStep">1</span></g:if>
                <g:else>

                    <div class="pagination left">
                        <g:paginate max="100" action="searchContigSets" params="${params}" total="${contigs.size()}" prev="&lt; previous" next="next &gt;"/>
                    </div>        <!-- .pagination ends -->

                </g:else>


                <g:form controller="contigSet" action="createFromSearch" method="post">
                    <g:hiddenField name="q" value="${query}"/>
                    <g:hiddenField name="contigList" value="${contigs*.id.join(',')}"/>
                    <g:hiddenField name="studyId" value="${studyId}"/>

                    <input type="submit" class="submit long" value="Save as contigSet"/>
                </g:form>
            </div><!-- .block_content ends -->
            <div class="bendl"></div>

            <div class="bendr"></div>

        </div>
    </g:if>

</body>
</html>