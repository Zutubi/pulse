[#ftl]

<#---------------------------------------------------------------------------
=============================================================================
Functions that return useful urls.
=============================================================================
----------------------------------------------------------------------------->


<#---------------------------------------------------------------------------
Returns a link to the projecty home page for the project the given build
belongs to.
---------------------------------------------------------------------------->
[#function projectLink result]
    [#return "${baseUrl}/currentBuild.action?id=${result.project.id?c}"/]
[/#function]

<#---------------------------------------------------------------------------
Returns a link to the summary page for a build result.
---------------------------------------------------------------------------->
[#function buildLink result]
    [#return "${baseUrl}/viewBuild.action?id=${result.id?c}"/]
[/#function]

<#---------------------------------------------------------------------------
Returns a link to the tests page for a build result.
---------------------------------------------------------------------------->
[#function testsLink result]
    [#return "${baseUrl}/viewTests.action?id=${result.id?c}"/]
[/#function]

<#---------------------------------------------------------------------------
Returns a link to the detailed view page for a build stage.
---------------------------------------------------------------------------->
[#function stageDetailsLink result node]
    [#return "${baseUrl}/viewCommandLog.action?id=${result.id?c}&amp;selectedNode=${node.id?c}"/]
[/#function]

<#---------------------------------------------------------------------------
Returns a link to the tests page for a build stage.
---------------------------------------------------------------------------->
[#function stageTestsLink result node]
    [#return "${baseUrl}/viewTestSuite.action?id=${result.id?c}&amp;nodeId=${node.id?c}"/]
[/#function]


<#---------------------------------------------------------------------------
=============================================================================
Macros that output plain text.
=============================================================================
----------------------------------------------------------------------------->


<#---------------------------------------------------------------------------
A macro to show the messages directly on the result object of the given
level.
---------------------------------------------------------------------------->
[#macro resultMessages result level indent=""]
    [#list result.getFeatures(level) as feature]
${indent}  * ${renderer.wrapString(feature.summary, "${indent}    ")}
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the file artifact as a flat list but
with context.
---------------------------------------------------------------------------->
[#macro fileArtifactMessages artifact level context]
    [#if artifact.hasMessages(level)]
  - ${renderer.wrapString("${context}", "    ")}
    ${renderer.wrapString("${artifact.path}", "    ")}
        [#list artifact.getFeatures(level) as feature]
    * ${renderer.wrapString(feature.summary, "      ")}
        [/#list]
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the artifact as a flat list but with
context.
---------------------------------------------------------------------------->
[#macro artifactMessages artifact level context]
    [#local fileContext="${context} :: ${artifact.name}"]
    [#list artifact.children as fileArtifact]
        [@fileArtifactMessages artifact=fileArtifact level=level context=fileContext/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the command result and its included
artifacts as a flat list but with context.
---------------------------------------------------------------------------->
[#macro commandResultMessages result level context]
    [#local nestedContext = "${context} :: ${result.commandName}"]
    [#if result.hasDirectMessages(level)]
  - ${renderer.wrapString(nestedContext, "    ")}
        [@resultMessages result=result level=level indent="  "/]
    [/#if]
    [#list result.artifacts as artifact]
        [@artifactMessages artifact=artifact level=level context=nestedContext/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the recipe result and its included
results as a flat list but with context.
---------------------------------------------------------------------------->
[#macro recipeResultMessages result level context]
    [#if result.hasDirectMessages(level)]
  - ${renderer.wrapString(context, "    ")}
        [@resultMessages result=result level=level indent="  "/]
    [/#if]
    [#list result.commandResults as commandResult]
        [@commandResultMessages result=commandResult level=level context=context/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the result node and its included
results as a flat list but with context.
---------------------------------------------------------------------------->
[#macro recipeNodeMessages node level context=""]
    [#if node.hasMessages(level)]
        [#if context?length &gt; 0]
            [#local nestedContext = "${context} :: stage ${node.stageName} :: ${node.result.recipeNameSafe}@${node.hostSafe}"]
        [#else]
            [#local nestedContext = "stage ${node.stageName} :: ${node.result.recipeNameSafe}@${node.hostSafe}"]
        [/#if]
        [@recipeResultMessages result=node.result level=level context=nestedContext/]
        [#list node.children as child]
            [@recipeNodeMessages node=child level=level context=nestedContext/]
        [/#list]
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the build result and its included
results as a flat list but with context.
---------------------------------------------------------------------------->
[#macro buildMessages result level]
    [#if result.hasMessages(level)]
${level?lower_case?cap_first} messages:
        [@resultMessages result=result level=level/]
        [#list result.root.children as child]
            [@recipeNodeMessages node=child level=level/]
        [/#list]
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
Outputs a list of build stage results for the given build.
---------------------------------------------------------------------------->
[#macro buildStages result]
    [#list result.root.children as child]
  * ${child.stageName} :: ${child.result.recipeNameSafe}@${child.hostSafe} :: ${child.result.state.prettyString}
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows a list of the changes made in a build.
---------------------------------------------------------------------------->
[#macro buildChanges]
    [#if changelists?exists]
        [#if changelists?size &gt; 0]
New changes in this build:
            [#list changelists as change]
                [#assign revision = change.revision]
  * ${revision.revisionString} by ${revision.author}:
    ${renderer.wrapString(renderer.trimmedString(revision.comment, 180), "    ")}
            [/#list]
        [#else]
There were no new changes in this build.
        [/#if]
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
A macro to show a failed test case as part of a flat plain text list.
---------------------------------------------------------------------------->
[#macro showTestCaseFailure test suiteContext=""]
    [#local testStatus = "${test.name} (${test.status?lower_case})"]
    [#if suiteContext?length &gt; 0]
        [#local testContext = "${suiteContext} :: ${testStatus}"]
    [#else]
        [#local testContext = testStatus]
    [/#if]
    * ${renderer.wrapString(testContext, "      ")}
    [#if test.message?exists]
      ${renderer.wrapString(test.message, "      ")}
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
A macro to show test suite failures as part of a flat plain text list.
---------------------------------------------------------------------------->
[#macro showTestSuiteFailures suite showContext=true suiteContext=""]
    [#if showContext]
        [#if suiteContext?length &gt; 0]
            [#local testContext = "${suiteContext} :: ${suite.name}"]
        [#else]
            [#local testContext = suite.name]
        [/#if]
    [#else]
        [#local testContext = suiteContext]
    [/#if]
    [#list suite.suites as childSuite]
        [@showTestSuiteFailures suite=childSuite suiteContext=testContext/]
    [/#list]
    [#list suite.cases as childCase]
        [@showTestCaseFailure test=childCase suiteContext=testContext/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
A macro to show the test summary in a recipe, include the failed tests as a
flat plain text list.
---------------------------------------------------------------------------->
[#macro recipeTestSummary result context]
    [#local summary = result.testSummary]
    [#if summary.total &gt; 0]
  - ${renderer.wrapString(context, "    ")}
    Test summary: total: ${summary.total}, errors: ${summary.errors}, failures: ${summary.failures}
    [/#if]
    [#if result.hasBrokenTests()]
        [#local excess = result.excessFailureCount/]
        [#if excess &gt; 0]
    NOTE: This recipe has ${excess} more failures, see the full test report
    for details.
        [/#if]
        [@showTestSuiteFailures suite=result.failedTestResults showContext=false/]
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
A macro to show the test summary for a recipe node, including the failed
tests as a flat plain text list.
---------------------------------------------------------------------------->
[#macro recipeNodeTestSummary node context=""]
    [#if context?length &gt; 0]
        [#local nestedContext = "${context} :: ${node.stageName} :: ${node.result.recipeNameSafe}@${node.hostSafe}"]
    [#else]
        [#local nestedContext = "${node.stageName} :: ${node.result.recipeNameSafe}@${node.hostSafe}"]
    [/#if]
    [#if node.result?exists]
        [@recipeTestSummary result=node.result context=nestedContext/]
    [/#if]
    [#list node.children as child]
        [@recipeNodeTestSummary node=child context=nestedContext/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
A macro to show test summary information for a build including the failed
tests as a flat plain text list.
---------------------------------------------------------------------------->
[#macro buildTestSummary result]
    [#local summary = result.testSummary]
    [#if summary.total &gt; 0]
Test summary: total: ${summary.total}, errors: ${summary.errors}, failures: ${summary.failures}
    [/#if]
    [#if !summary.allPassed()]
        [#list result.root.children as child]
            [@recipeNodeTestSummary node=child/]
        [/#list]
    [/#if]
[/#macro]


<#---------------------------------------------------------------------------
=============================================================================
HTML-specific macros
=============================================================================
----------------------------------------------------------------------------->


<#---------------------------------------------------------------------------
Outputs inline style definitions for HTML result emails.
---------------------------------------------------------------------------->
[#macro stylesHTML]
    <style type="text/css">
    <!--
body {
    font: sans-serif;
}

h1 {
    font-size: 130%;
}

h3 {
    margin-top: 0px;
    font-size: 100%;
}

.small {
    font-size: 80%;
}

a:link, a:visited {
    color: #668;
    text-decoration: none;
}

a:hover {
    color: #336;
    border-bottom: solid 2px #336;
}

a {
    font-weight: bold;
    text-decoration: none;
    border-bottom: dotted 1px #336;
}

a.unadorned {
    border-bottom: none;
}

table.content {
    border-collapse: collapse;
    border: 1px solid #bbb;
    margin-bottom: 16px;
}

th.heading, th.content, td.content, td.content-right, td.failure, td.error, td.test-failure, td.success {
    border: 1px solid #bbb;
    padding: 4px;
    text-align: left;
    vertical-align: top;
}

th.heading {
    background: #e9e9f5;
}

td.success {
    color: #383;
    background: #ffffff;
}

td.failure, td.error, td.test-failure {
    color: #b22;
    font-weight: bold;
}

td.failure, td.error {
    background: #fff0f0;
}


ul {
    margin-left: 20px;
    padding-left: 20px;
    padding-right: 20px;
}

ul.error, ul.warning, ul.info {
    border: solid 1px;
}

ul.error {
    border-color: #c00000;
    background-color: #ffdddd;
}

ul.warning {
    border-color: #f0c000;
    background-color: #ffffce;
}

ul.info {
    border-color: #b0b0f0;
    background-color: #eeeeff;
}

li.error, li.warning, li.info {
    font-weight: normal;
    margin-top: 4px;
    margin-bottom: 4px;
}

li.header {
    font-family: sans-serif;
    font-weight: bold;
    margin-top: 4px;
    margin-bottom: 4px;
}

pre.feature {
    font-family: sans-serif;
    margin-top: 8px;
    margin-bottom: 0px;
}
    -->
    </style>
[/#macro]

<#---------------------------------------------------------------------------
Shows a list of links for a build.
---------------------------------------------------------------------------->
[#macro buildLinksHTML result]
    <p style="font-size: 85%">
        jump to ::
          <a href="${baseUrl}/viewBuild.action?id=${result.id?c}">summary</a>
        | <a href="${baseUrl}/viewChanges.action?id=${result.id?c}">changes</a>
        | <a href="${baseUrl}/viewTests.action?id=${result.id?c}">tests</a>
        | <a href="${baseUrl}/viewBuildArtifacts.action?id=${result.id?c}">artifacts</a>
    <p/>
[/#macro]

<#---------------------------------------------------------------------------
Shows a summary table for a build.
---------------------------------------------------------------------------->
[#macro buildSummaryHTML result]
    [@openTable/]
        [@headingRow heading="summary" span=8/]
        <tr>
            [@contentHeader cc="id"/]
            [@contentHeader cc="status"/]
            [@contentHeader cc="reason"/]
            [@contentHeader cc="tests"/]
            [@contentHeader cc="start time"/]
            [@contentHeader cc="end time"/]
            [@contentHeader cc="elapsed"/]
        </tr>
        <tr>
            [#assign class = result.state.string]
            [@linkCell cc=result.number?c url="${baseUrl}/viewBuild.action?id=${result.id?c}" class=class/]
            [@classCell cc=result.stateName?lower_case/]
            [@classCell cc=result.reason.summary/]
            [@linkCell cc=result.testSummary url="${testsLink(result)}" class=class/]
            [@classCell cc=result.stamps.prettyStartDate/]
            [@classCell cc=result.stamps.prettyEndDate/]
            [@classCell cc=result.stamps.prettyElapsed/]
        </tr>
    </table>
[/#macro]

<#---------------------------------------------------------------------------
Shows a list of links to build stage logs.
---------------------------------------------------------------------------->
[#macro stageLogLinksHTML result]
    <p style="font-size: 85%">
        stage logs ::
    [#list result.root.children as child]
        <a href="${baseUrl}/tailRecipeLog.action?id=${child.id?c}&buildId=${result.id?c}">${child.stageName}</a> [#if child_has_next]|[/#if]
    [/#list]
    </p>
[/#macro]

<#---------------------------------------------------------------------------
Shows a summary for each stage in a build.
---------------------------------------------------------------------------->
[#macro buildStageSummariesHTML result]
    [@openTable/]
        [@headingRow heading="stages" span=8/]
        <tr>
            [@contentHeader cc="stage"/]
            [@contentHeader cc="recipe"/]
            [@contentHeader cc="host"/]
            [@contentHeader cc="status"/]
            [@contentHeader cc="tests"/]
            [@contentHeader cc="start time"/]
            [@contentHeader cc="end time"/]
            [@contentHeader cc="elapsed"/]
        </tr>
    [#list result.root.children as child]
        <tr>
            [#assign class = child.result.state.string]
            [@classCell cc=child.stageName/]
            [@linkCell cc=child.stageName url="${stageDetailsLink(result, child)}" class=class/]
            [@classCell cc=child.result.recipeNameSafe/]
            [@classCell cc=child.hostSafe/]
            [@classCell cc=child.result.stateName?lower_case/]
            [@linkCell cc=child.testSummary url="${stageTestsLink(result, child)}" class=class/]
            [@classCell cc=result.stamps.prettyStartDate/]
            [@classCell cc=result.stamps.prettyEndDate/]
            [@classCell cc=child.result.stamps.prettyElapsed/]
        </tr>
    [/#list]
    </table>
[/#macro]

<#---------------------------------------------------------------------------
Shows a table with the given changelists.
---------------------------------------------------------------------------->
[#macro buildChangesHTML changelists trim=true limit=60]
    [@openTable/]
        [@headingRow heading="changes" span=5/]
        <tr>
            [@contentHeader cc="revision"/]
            [@contentHeader cc="who"/]
            [@contentHeader cc="when"/]
            [@contentHeader cc="comment"/]
            [@contentHeader cc="actions"/]
        </tr>
    [#if changelists?size &gt; 0]
        [#list changelists as change]
        <tr>
            [@dynamicCell cc=change.revision.revisionString/]
            [@dynamicCell cc=change.user/]
            [@dynamicCell cc=change.prettyTime/]
            [#if trim ]
            [@contentCell cc=renderer.transformComment(change, limit)/]
            [#else]
            [@contentCell cc=renderer.transformCommentWithoutTrimming(change)/]
            [/#if]
            [@linkCell cc="view" url="${baseUrl}/viewChangelist.action?id=${change.id?c}&amp;buildId=${result.id?c}"/]
        </tr>
        [/#list]
    [#else]
        <tr>
            [@contentCell cc="no changes in this build" span=5/]
        <tr>
    [/#if]
    </table>
[/#macro]

<#---------------------------------------------------------------------------
A macro to show the messages directly on the result object of the given
level as HTML list elements.
---------------------------------------------------------------------------->
[#macro resultMessagesHTML result level]
    [#list result.getFeatures(level) as feature]
<li class="${level?lower_case}"><pre class="feature">${feature.summary?html}</pre></li>
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the file artifact as a HTML nested
list.
---------------------------------------------------------------------------->
[#macro fileArtifactMessagesHTML result command artifact level]
    [#if artifact.hasMessages(level)]
<li class="header">artifact :: ${artifact.path?html}
    <ul>
        [#list artifact.getFeatures(level) as feature]
        <li class="${level?lower_case}"><pre class="feature">${feature.summary?html}</pre>
            [#if feature.isPlain()]
                <a class="unadorned" href="${baseUrl}/viewArtifactt.action?id=${artifact.id?c}&amp;buildId=${result.id?c}&amp;commandId=${command.id?c}#${feature.firstLine?c}">
                    <span class="small">jump to &gt;&gt;</span>
                </a>
            [/#if]
        </li>
        [/#list]
    </ul>
</li>
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the artifact as a HTML nested list.
---------------------------------------------------------------------------->
[#macro artifactMessagesHTML result command artifact level]
    [#list artifact.children as fileArtifact]
        [@fileArtifactMessagesHTML result=result command=command artifact=fileArtifact level=level/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the command result and its included
artifacts as HTML nested list.
---------------------------------------------------------------------------->
[#macro commandResultMessagesHTML result command level]
    [@resultMessagesHTML result=command level=level/]
    [#list command.artifacts as artifact]
        [@artifactMessagesHTML result=result command=command artifact=artifact level=level/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the recipe result and its included
results as a HTML nested list.
---------------------------------------------------------------------------->
[#macro recipeResultMessagesHTML result recipe level]
    [@resultMessagesHTML result=recipe level=level/]
    [#list recipe.commandResults as command]
        [#if command.hasMessages(level)]
<li class="header">command :: ${command.commandName?html}
    <ul>
        [@commandResultMessagesHTML result=result command=command level=level/]
    </ul>
</li>
        [/#if]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the result node and its included
results as a HTML nested list.
---------------------------------------------------------------------------->
[#macro recipeNodeMessagesHTML result node level]
    [#if node.hasMessages(level)]
<li class="header">stage ${node.stageName?html} :: ${node.result.recipeNameSafe?html}@${node.hostSafe?html}
    <ul>
        [@recipeResultMessagesHTML result=result recipe=node.result level=level/]
        [#list node.children as child]
            [@recipeNodeMessagesHTML result=result node=child level=level/]
        [/#list]
    </ul>
</li>
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the build result and its included
results as a HTML nested list.
---------------------------------------------------------------------------->
[#macro buildMessagesHTML result level]
    [#if result.hasMessages(level)]
<h3 style="font-size: 100%">${level?lower_case} messages</h3>
<ul class="${level?lower_case}">
        [@resultMessagesHTML result=result level=level/]
        [#list result.root.children as child]
            [@recipeNodeMessagesHTML result=result node=child level=level/]
        [/#list]
</ul>
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
Shows failing tests from a test suite.
---------------------------------------------------------------------------->
[#macro showSuiteFailuresHTML test showSummary=true indent=""]
    [#if showSummary]
    <tr>
        [#assign class = "test-failure"]
        [@classCell cc="${indent}${test.name?html}"/]
        [@contentCell cc="&nbsp;"/]
        [@contentCell cc="&nbsp;"/]
    </tr>
        [#local nestedIndent="${indent}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"/]
    [#else]
        [#local nestedIndent=indent/]
    [/#if]
    [#list test.suites as childSuite]
        [@showSuiteFailuresHTML test=childSuite indent=nestedIndent/]
    [/#list]
    [#list test.cases as childCase]
        [@showCaseFailureHTML test=childCase indent=nestedIndent/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Outputs a single failing test case.
---------------------------------------------------------------------------->
[#macro showCaseFailureHTML test indent=""]
    <tr>
    [#assign class = "test-failure"]
        [@classCell cc="${indent}${test.name?html}"/]
        [@classCell cc=test.status?lower_case/]
    [#if test.message?exists]
        [@openCell/]
                <pre>${test.message?html}</pre>
        </td>
    [#else]
        [@contentCell cc="&nbsp;"/]
    [/#if]
    </tr>
[/#macro]

<#---------------------------------------------------------------------------
Outputs failing test cases for the given recipe node.
---------------------------------------------------------------------------->
[#macro recipeNodeFailedTestsHTML node]
    [#if node.result?exists]
        [#if node.result.hasBrokenTests() && node.result.failedTestResults?exists]
            [#local summary = node.result.testSummary/]
                <tr><td>
                    [@openTable/]
                        [@headingRow heading="stage ${node.stageName} :: broken tests (total: ${summary.total}, errors: ${summary.errors}, failures: ${summary.failures})" span=3/]
            [#local excess = node.result.excessFailureCount/]
            [#if excess &gt; 0]
                        <tr><th colspan="3" style="background-color: #ffffc0">This recipe has ${excess} further test failures, see the full test report for details.</th></tr>
            [/#if]
                        <tr>
                            [@contentHeader cc="test"/]
                            [@contentHeader cc="status"/]
                            [@contentHeader cc="details"/]
                        </tr>
            [@showSuiteFailuresHTML test=node.result.failedTestResults showSummary=false/]
                    </table>
                </td></tr>
        [/#if]
    [/#if]
    [#list node.children as child]
        [@recipeNodeFailedTestsHTML node=child/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Outputs failing test cases for the given build.
---------------------------------------------------------------------------->
[#macro buildFailedTestsHTML result]
    [#list result.root.children as child]
        [@recipeNodeFailedTestsHTML node=child/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Opens a table, adding embedded styles.
---------------------------------------------------------------------------->
[#macro openTable]
    <table class="content" style="border-collapse: collapse; border: 1px solid #bbb; margin-bottom: 16px;">
[/#macro]

<#---------------------------------------------------------------------------
Opens a table cell, adding embedded styles.
---------------------------------------------------------------------------->
[#macro openCell type="td" class="content" span=1]
    <${type} class="${class}" colspan="${span}" style="border: 1px solid #bbb; padding: 3px; text-align: left;">
[/#macro]

<#---------------------------------------------------------------------------
A content header cell
---------------------------------------------------------------------------->
[#macro contentHeader cc span=1]
    [@openCell type="th" span=span/]${cc}</th>
[/#macro]

<#---------------------------------------------------------------------------
A content cell
---------------------------------------------------------------------------->
[#macro contentCell cc span=1]
    [@openCell span=span/]${cc}</td>
[/#macro]

<#---------------------------------------------------------------------------
A content cell with content escaped HTML-wise
---------------------------------------------------------------------------->
[#macro dynamicCell cc]
    [@openCell/]${cc?html}</td>
[/#macro]

<#---------------------------------------------------------------------------
A content cell with CSS class set to the value of $class
---------------------------------------------------------------------------->
[#macro classCell cc]
    [@openCell class="${class}"/]${cc}</td>
[/#macro]

<#---------------------------------------------------------------------------
A content cell with a link
---------------------------------------------------------------------------->
[#macro linkCell cc url class="content"]
    [@openCell class=class/]<a href="${url}">${cc}</a></td>
[/#macro]

<#---------------------------------------------------------------------------
A heading row to top a table.
---------------------------------------------------------------------------->
[#macro headingRow heading span]
    <tr>
        <th class="heading" colspan="${span}" style="border: 1px solid #bbb; padding: 3px; text-align: left; vertical-align: top; background: #e9e9f5;">
            ${heading}
        </th>
    </tr>
[/#macro]

<#---------------------------------------------------------------------------
A row in a project summary notification table.
---------------------------------------------------------------------------->
[#macro summaryRow ch cc url="" colour=""]
    <tr>
        <th style="text-align: right; background: #fafae8; color: #124; border-right: solid 1px #124; padding: 4px">${ch}</th>
    [#if colour?length &gt; 0]
        <td style="padding: 4px; color: ${colour}">
    [#else]
        <td style="padding: 4px">
    [/#if]
            [#if url?length &gt; 0]<a href="${url}" style="font-weight: bold; color: #336; text-decoration: none; border-bottom: dotted 1px #336;">[/#if]
                ${cc}
            [#if url?length &gt; 0]</a>[/#if]
        </td>
    </tr>
[/#macro]
