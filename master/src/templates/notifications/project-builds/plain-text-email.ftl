[#ftl]
=====================================================================
:: ${result.project.name} ::
=====================================================================
Build ${result.number?c} has completed with status '${result.state.prettyString}'.

You can view the full build result at:

${buildLink(result)}

[#if result.reason?exists]
Build reason: ${result.reason.summary}.

[/#if]
Build stages:
[@buildStages result=result/]

[@buildChanges/]

[@buildMessages result=result level=errorLevel/]

[@buildMessages result=result level=warningLevel/]

[@buildTestSummary result=result/]
