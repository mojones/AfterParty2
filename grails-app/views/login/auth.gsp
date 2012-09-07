<head>
    <title>Log in</title>
    <meta name="layout" content="main.gsp"/>
</head>

<body>
<div class="row-fluid">
    <div class="span10 offset1">
        <h2>Log in</h2>

        <form action='${postUrl}' method='POST' id="loginForm" name="loginForm" autocomplete='off'>
            <label>Username:</label>
            <input type="text" class="text small" name="j_username" placeholder="type your username"/>
            <label>Password:</label>
            <input type="text" class="text small" name="j_password"  placeholder="type your password"/>
            <br/>
            <button type="submit" class="btn btn-info"><i class="icon-user"></i>&nbsp;log in</button>

        </form>
    </div>        <!-- .block_content ends -->

</div>



<script>
    $(document).ready(function() {
        $('#username').focus();
    });

    <s2ui:initCheckboxes/>

</script>

</body>
