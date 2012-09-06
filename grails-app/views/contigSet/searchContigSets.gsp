<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.springframework.util.ClassUtils" %>

<html>
<head>
    <meta name="layout" content="main"/>

    <title>Search contigs | ${query}</title>

</head>

<body>

<div class="row-fluid">
    <div class="span10 offset1">
        <g:if test="${contigs.size() > 0}">
               <h2>
                    Showing ${contigs.size()} results for ${query}
                </h2>
                <g:form controller="contigSet" action="createFromContigList" method="post">
                    <g:hiddenField name="q" value="${query}"/>
                    <g:hiddenField name="contigList" value="${contigIdList.join(',')}"/>
                    <g:hiddenField name="studyId" value="${studyId}"/>

                    <button type="submit" class="btn btn-info"><i class="icon-tags"></i>&nbsp;save as contig set</button>
                </g:form>
                <g:render template="staticContigTable" model="['contigs' : contigs]"/>
        </g:if>
        <g:else>
            <h2>
                No results for ${query}, hit back to try again.
            </h2>
        </g:else>
    </div>
</div>
</body>
</html>