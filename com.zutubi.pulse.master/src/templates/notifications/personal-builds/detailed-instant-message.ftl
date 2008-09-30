[#ftl]
Project: ${result.project.name}
Personal build ${result.number} (${buildLink(result)})

Completed with status: ${result.state.prettyString}

Build stages:
[@buildStages result=result/]

[#if result.testSummary.total &gt; 0]
Test summary: ${result.testSummary} (${testsLink(result)})
[/#if]
