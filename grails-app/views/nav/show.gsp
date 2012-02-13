<li>
    <b>Navigation</b>
    <g:if test="${study}">
        <ul>
            <li><g:link controller="study" action="show" id="${study.id}">go to study</g:link> </li>
            <li><a href="#"><b>Compound Samples</b></a></li>
            <g:each in="${study.compoundSamples}" var="compoundSample">
                <li>
                    <g:link controller="compoundSample" action="show" id="${compoundSample.id}">
                        ${compoundSample.name}
                    </g:link>
                    <ul>
                        <li><a href="#"><b>Samples</b></a></li>
                        <g:each in="${compoundSample.samples}" var="sample">

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
                        <g:each in="${compoundSample.assemblies}" var="assembly">

                            <li>
                                <g:link controller="assembly" action="show" id="${assembly.id}">
                                    ${assembly.name}
                                </g:link>
                            </li>
                        </g:each>
                    </ul>
                </li>
            </g:each>:
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



