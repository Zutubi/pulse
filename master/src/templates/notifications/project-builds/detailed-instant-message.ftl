[#ftl]
${result.project.name} (${projectLink(result)})
Build ${result.number} (${buildLink(result)})

Completed with status: ${result.state.prettyString}

[#if result.reason?exists]
Build reason: ${result.reason.summary}.

[/#if]
Build stages:
[@buildStages result=result/]

[@buildChanges/]

[#if result.testSummary.total &gt; 0]
Test summary: ${result.testSummary} (${testsLink(result)})
[/#if]
