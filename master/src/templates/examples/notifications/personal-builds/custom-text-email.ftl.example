[#ftl]
=====================================================================
:: ${result.project.name} ::
=====================================================================
Personal build ${result.number?c} has completed with status '${result.state.prettyString}'.

You can view the full build result at:

${buildLink(result)}

Build stages:
[@buildStages result=result/]

[@buildMessages result=result level=errorLevel/]

[@buildMessages result=result level=warningLevel/]

[@buildTestSummary result=result/]
