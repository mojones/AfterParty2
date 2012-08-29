<li class="dropdown">
    <a id="drop1" class="dropdown-toggle" data-toggle="dropdown" role="button" href="#">
        <i class="icon-th-list icon-white"></i>&nbsp;My studies
        <b class="caret"></b>
    </a>
    <ul class="dropdown-menu" aria-labelledby="drop1" role="menu">
        <g:each in="${studies}" var="study">
            <li>
                
                <g:link controller="study" action="show" id="${study.id}">
                    <i class="icon-th-list"></i>&nbsp;<g:truncate maxlength="20">${study.name}</g:truncate>
                </g:link>

            </li>
        </g:each>
        <sec:ifLoggedIn>
            <li class="divider"></li>
            <li>
                <g:link controller="study" action="create" elementId="newStudyLink"><i class="icon-plus-sign"></i>&nbsp;Add New ...</g:link>
            </li>
        </sec:ifLoggedIn>
    </ul>
</li>

