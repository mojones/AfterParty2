<li>
    <b>Navigation</b>
    <g:if test="${study}">
        <ul>
            <li><a href="#"><b>Samples</b></a></li>
            <g:each in="${study.samples}" var="sample">

                <li>
                    <g:link controller="sample" action="show" id="${sample.id}">
                        ${sample.name}
                    </g:link>
                    <ul>
                        <li><a href="#"><b>Experiments</b></a></li>
                        <g:each in="${sample.experiments}" var="experiment">
                            <li>
                                <g:link controller="experiment" action="show" id="${experiment.id}">
                                    ${experiment.name}
                                </g:link>
                                <ul>
                                    <li><a href="#"><b>Runs</b></a></li>
                                    <g:each in="${experiment.runs}" var="run">
                                        <li>
                                            <g:link controller="run" action="show" id="${run.id}">
                                                ${run.name}
                                            </g:link>
                                        </li>
                                    </g:each>

                                </ul>

                            </li>
                        </g:each>

                    </ul>
                </li>
            </g:each>
            <li><a href="#"><b>Assemblies</b></a></li>
            <g:each in="${study.assemblies}" var="assembly">

                <li>
                    <g:link controller="assembly" action="show" id="${assembly.id}">
                        ${assembly.name}
                    </g:link>
                </li>
            </g:each>
        </ul>
    </g:if>
    <g:else>
        <ul>
            <li>
                <a href="#"><b>Select a study...</b></a>
            </li>
        </ul>
    </g:else>
</li>


