<head>
    <title><g:message code='spring.security.ui.login.title'/></title>
    <meta name="layout" content="main.gsp"/>
</head>

<body>

<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Log in</h2>
    </div>        <!-- .block_head ends -->

    <div class="block_content">
        <form action='${postUrl}' method='POST' id="loginForm" name="loginForm" autocomplete='off'>


            <p>
                %{--<label for="username"></label>--}%
                %{--<input name="j_username" id="username" size="20"/>--}%

                <label>Username:</label><br/>
                <input type="text" class="text small" name="j_username"/>
            </p>

            <p>
                <label>Password:</label><br/>
                <input type="text" class="text small" name="j_password"/>

            </p>
            <input type="submit" class="submit mid" value="Log in"/>

        </form>
    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>



<script>
    $(document).ready(function() {
        $('#username').focus();
    });

    <s2ui:initCheckboxes/>

</script>

</body>
