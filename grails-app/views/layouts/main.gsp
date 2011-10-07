<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <meta http-equiv="X-UA-Compatible" content="IE=7"/>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

    <title><g:layoutTitle default="Grails"/></title>

    %{--<style type="text/css" media="all">--}%
    %{--@import url("css/style.css");--}%
    %{--@import url("css/jquery.wysiwyg.css");--}%
    %{--@import url("css/facebox.css");--}%
    %{--@import url("css/visualize.css");--}%
    %{--@import url("css/date_input.css");--}%
    %{--</style>--}%

    <link rel="stylesheet" href="${resource(dir: 'css', file: 'style.css')}"/>

    %{--main jquery js file--}%
    <g:javascript library="jquery" plugin="jquery"/>

    %{-- adminus stuff --}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.img.preload.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.visualize.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.tablesorter.min.jsvis')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'facebox.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.select_skin.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.filestyle.mini.js')}"></script>


    %{--jquery edit in place plugin--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.editinplace.js')}"></script>


    %{--application-specific scripts--}%
    <g:javascript library="application"/>

    <g:layoutHead/>

</head>


<body>

<div id="hld">

    <div class="wrapper"><!-- wrapper begins -->



        <div id="header">
            <div class="hdrl"></div>

            <div class="hdrr"></div>

            <h1><a href="#">AfterParty</a></h1>

            <ul id="nav">

                <g:include controller="nav" action="show"/>


                <g:include controller="nav" action="showStudies"/>


                <li><a href="#"><b>Search</b></a>
                    <g:if test="${session.studyId}">
                        <ul>
                            <li><b><g:link controller="contig" action="search">Contigs</g:link></b></li>
                        </ul>
                    </g:if>
                    <g:else>
                        <ul>
                            <li>
                                <g:link controller="study"><b>Select a study...</b></g:link>
                            </li>
                        </ul>
                    </g:else>
                </li>

            </ul>

            <p class="user">Hello, <a href="#">John</a> | <a href="index.html">Logout</a></p>
        </div>        <!-- #header ends -->


    <g:layoutBody/>

</body>
</html>