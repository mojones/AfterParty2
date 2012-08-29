<li class="dropdown">
    <a id="study-drop" class="dropdown-toggle" data-toggle="dropdown" role="button" href="#">
    <i class="icon-folder-open icon-white"></i>&nbsp;Navigation  <b class="caret"></b>
    </a>
    <g:if test="${study}">
        <ul class="dropdown-menu" aria-labelledby="study-drop" role="menu">
            <li><g:link controller="study" action="show" id="${study.id}">Go to study</g:link> </li>
            <li class="divider"></li>
            <li><a href="#"><b>Key</b></a></li>
            <li><a href="#"><i class="icon-leaf"></i>&nbsp;Compound Samples</a></li>
            <li><a href="#"><i class="icon-tag"></i>&nbsp;Samples</a></li>
            <li><a href="#"><i class="icon-tint"></i>&nbsp;Experiments</a></li>
            <li><a href="#"><i class="icon-cog"></i>&nbsp;Runs</a></li>
            <li><a href="#"><i class="icon-align-left"></i>&nbsp;Assemblies</a></li>

            
            <li class="divider"></li>

            <g:each in="${study.compoundSamples}" var="compoundSample">
                <li>
                    <g:link controller="compoundSample" action="show" id="${compoundSample.id}">
                        <i class="icon-leaf"></i>&nbsp;${compoundSample.name}
                    </g:link>
                    <ul>
                        <g:each in="${compoundSample.samples}" var="sample">

                            <li style="list-style: none;">
                                <g:link controller="sample" action="show" id="${sample.id}">
                                    <i class="icon-tag"></i>&nbsp;${sample.name}
                                </g:link>
                                <ul>
                                    <g:each in="${sample.experiments}" var="experiment">
                                        <li style="list-style: none;">
                                            <g:link controller="experiment" action="show" id="${experiment.id}">
                                                <i class="icon-tint"></i>&nbsp;${experiment.name}
                                            </g:link>
                                            <ul>
                                                <g:each in="${experiment.runs}" var="run">
                                                    <li style="list-style: none;">
                                                        <g:link controller="run" action="show" id="${run.id}">
                                                            <i class="icon-cog"></i>&nbsp;${run.name}
                                                        </g:link>
                                                    </li>
                                                </g:each>

                                            </ul>

                                        </li>
                                    </g:each>

                                </ul>
                            </li>
                        </g:each>
                        <g:each in="${compoundSample.assemblies}" var="assembly">

                            <li style="list-style: none;">
                                <g:link controller="assembly" action="show" id="${assembly.id}">
                                    <i class="icon-align-left"></i>&nbsp;${assembly.name}
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



