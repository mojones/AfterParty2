<li>
    <b>Studies</b>
    <ul>
        <g:each in="${studies}" var="study">
            <li>
                <g:link controller="study" action="show" id="${study.id}">
                    <g:truncate maxlength="20">${study.name}</g:truncate>
                </g:link>

            </li>
        </g:each>
        <sec:ifLoggedIn>
            <li>
                <g:link controller="study" action="create" elementId="newStudyLink">Add New ...</g:link>
            </li>
        </sec:ifLoggedIn>
    </ul>
</li>

