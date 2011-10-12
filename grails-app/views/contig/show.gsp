<%@ page import="afterparty.Contig" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main.gsp"/>
    <g:set var="entityName" value="${message(code: 'contig.label', default: 'Contig')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>

    %{--raphael library included on this page to show contig annotations, also g plugin and line plugin--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.raphael-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'g.line-min.js')}"></script>

</head>

<body>

<p>
    <!-- AddToAny BEGIN -->
    <a class="a2a_dd" href="http://www.addtoany.com/share_save"><img
            src="http://static.addtoany.com/buttons/share_save_256_24.png" width="256" height="24" border="0"
            alt="Share"/></a>
    <script type="text/javascript" src="http://static.addtoany.com/menu/page.js"></script>
    <!-- AddToAny END -->
</p>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>${contigInstance.name}</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <h3>Read count : ${contigInstance.readCount}</h3>

        <p>tags : ${contigInstance.tags.join(',')}</p>

        <h3>Sequence</h3>
        <textarea rows="10" cols="100">${contigInstance.sequence}</textarea>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Annotation</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <script type="text/javascript"
                src="<g:createLink controller="contig" action="annotation" id="${contigInstance.id}"/>"></script>

        <div id="annotation"></div>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>BLAST hits</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">

        <table cellpadding="0" cellspacing="0" width="100%" class="sortable">

            <thead>
            <tr>
                <th>Accession</th>
                <th>Bitscore</th>
                <th>Decription</th>
                <th>Start</th>
                <th>Stop</th>

            </tr>
            </thead>

            <tbody>
            <g:each in="${contigInstance.blastHits.sort({-it.bitscore})}" var="b">
                <tr id="${b.accession}">
                    <td>${b.accession}</td>
                    <td>${b.bitscore}</td>
                    <td>${b.description}</td>
                    <td>${b.start}</td>
                    <td>${b.stop}</td>
                </tr>
            </g:each>
            </tbody>

        </table>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>

</body>
</html>
