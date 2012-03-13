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
                    Showing ${contigs.size()} results for ${query}
                </h2>

            </div>        <!-- .block_head ends -->

            <div class="block_content">

                <g:render template="contigTable" model="['contigCollection' : contigs, 'contigsPerPage' : 30]"/>
                <p>

                    <g:form controller="contigSet" action="createFromContigList" method="post">
                        <g:hiddenField name="q" value="${query}"/>
                        <g:hiddenField name="contigList" value="${contigs*.id.join(',')}"/>
                        <g:hiddenField name="studyId" value="${studyId}"/>

                        <input type="submit" class="submit long" value="Save as contigSet"/>
                    </g:form>
                </p>
            </div><!-- .block_content ends -->
            <div class="bendl"></div>

            <div class="bendr"></div>

        </div>
    </g:if>

</body>
</html>