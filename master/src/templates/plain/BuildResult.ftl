[#ftl]
=====================================================================
:: ${result.project.name} ::
=====================================================================
Build ${result.number?c} has completed with status '${result.state.prettyString}'.

You can view the full build result at:

http://${hostname}/viewBuild.action?id=${result.id?c}

[#if result.scmDetails?exists]
    [#assign changes = result.scmDetails.changelists]
    [#if changes?size &gt; 0]
New changes in this build:
        [#list changes as change]
            [#assign revision = change.revision]
  * ${revision.revisionString} by ${revision.author}:
    ${renderer.trimmedString(revision.comment, 60)}
        [/#list]
    [#else]
There were no new changes in this build.
    [/#if]
[/#if]

[@buildMessages result=result level=errorLevel/]

[@buildMessages result=result level=warningLevel/]
