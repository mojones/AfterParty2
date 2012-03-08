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
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.select_skin.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.filestyle.mini.js')}"></script>


    %{--jquery edit in place plugin--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.editinplace.js')}"></script>

    %{--jquery smart paginator stuff--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'smartpaginator.js')}"></script>
    <link rel="stylesheet" href="${resource(dir: 'js', file: 'smartpaginator.css')}"/>

    %{--table sorter--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.tablesorter.min.js')}"></script>

    %{--load mask for long running stuff--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.loadmask.min.js')}"></script>
    <link rel="stylesheet" href="${resource(dir: 'js', file: 'jquery.loadmask.css')}"/>


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

            <h1><a href="/">AfterParty</a></h1>

            <ul id="nav">

                <g:if test="${session.studyId}">
                    <g:include controller="nav" action="show"/>
                </g:if>

                <sec:ifLoggedIn>
                    <g:include controller="nav" action="showStudies"/>
                </sec:ifLoggedIn>

            </ul>
            <sec:ifLoggedIn>
                <p class="user">Hello, <strong><sec:username/></strong> | <g:link controller="logout"
                                                                                  action="index">Logout</g:link></p>
            </sec:ifLoggedIn>
            <sec:ifNotLoggedIn>
                <p class="user"><g:link controller="login" action="index">Click here to log in</g:link></p>
            </sec:ifNotLoggedIn>

        </div>        <!-- #header ends -->

        <g:if test="${flash.success}">
            <div class="message success"><p>${flash.success}</p></div>
        </g:if>
        <g:if test="${flash.error}">
            <div class="message errormsg"><p>${flash.error}</p></div>
        </g:if>

        <p id="log"></p>

        <g:layoutBody/>

</body>
</html>