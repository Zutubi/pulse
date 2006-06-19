[#ftl]

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
            [#local nestedContext = "${context} :: stage ${node.stage?html} :: ${node.result.recipeNameSafe}@${node.hostSafe}"]
        [#else]
            [#local nestedContext = "stage ${node.stage?html} :: ${node.result.recipeNameSafe}@${node.hostSafe}"]
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
A macro to show failed test results as a flat plain text list.
---------------------------------------------------------------------------->
[#macro showTestFailures test suiteContext=""]
    [#if test.hasBrokenTests()]
        [#if test.isSuite()]
            [#local testStatus = test.name]
        [#else]
            [#local testStatus = "${test.name} (${test.status?lower_case})"]
        [/#if]
        [#if suiteContext?length &gt; 0]
            [#local testContext = "${suiteContext} :: ${testStatus}"]
        [#else]
            [#local testContext = testStatus]
        [/#if]
    * ${renderer.wrapString(testContext, "      ")}
        [#if test.isSuite()]
      ${renderer.wrapString("total: ${test.total}, errors: ${test.errors}, failures: ${test.failures}", "      ")}
            [#list test.children as child]
                [@showTestFailures test=child suiteContext=testContext/]
            [/#list]
        [#else]
            [#if test.message?exists]
      ${renderer.wrapString(test.message, "      ")}
            [/#if]
        [/#if]
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
A macro to show the failed tests for a file artifact flat plain text list.
---------------------------------------------------------------------------->
[#macro fileArtifactFailedTests artifact]
    [#list artifact.tests as test]
        [@showTestFailures test=test/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
A macro to show the failed tests in a recipe as a flat plain text list.
---------------------------------------------------------------------------->
[#macro recipeFailedTests result context]
    [#list result.commandResults as command]
        [#local commandContext = "${context} :: ${command.commandName}"]
        [#list command.artifacts as artifact]
            [#local artifactContext="${commandContext} :: ${artifact.name}"]
            [#list artifact.children as fileArtifact]
                [#if fileArtifact.hasBrokenTests()]
  - ${renderer.wrapString(artifactContext, "    ")}
    ${renderer.wrapString("${fileArtifact.path}", "    ")}
                    [@fileArtifactFailedTests artifact=fileArtifact/]
                [/#if]
            [/#list]
        [/#list]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
A macro to show the failed tests in a recipe node as a flat plain text list.
---------------------------------------------------------------------------->
[#macro recipeNodeFailedTests node context=""]
    [#if context?length &gt; 0]
        [#local nestedContext = "${context} :: ${node.stage} :: ${node.result.recipeNameSafe}@${node.hostSafe}"]
    [#else]
        [#local nestedContext = "${node.stage} :: ${node.result.recipeNameSafe}@${node.hostSafe}"]
    [/#if]
    [#if node.result?exists]
        [@recipeFailedTests result=node.result context=nestedContext/]
    [/#if]
    [#list node.children as child]
        [@recipeNodeFailedTests node=child context=nestedContext/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
A macro to show the failed tests in a build as a flat plain text list.
---------------------------------------------------------------------------->
[#macro buildFailedTests result]
    [#local summary = result.testSummary]
    [#if !summary.allPassed()]
Broken tests (total: ${summary.total}, errors: ${summary.errors}, failures: ${summary.failures}):
        [#list result.root.children as child]
            [@recipeNodeFailedTests node=child/]
        [/#list]
    [/#if]
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
                <a class="unadorned" href="http://${hostname}/viewArtifact.action?id=${artifact.id?c}&amp;buildId=${result.id?c}&amp;commandId=${command.id?c}#${feature.firstLine?c}">
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
<li class="header">stage ${node.stage?html} :: ${node.result.recipeNameSafe?html}@${node.hostSafe?html}
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

[#macro showTestHTML test indent=""]
    [#if test.hasBrokenTests()]
        <tr>
        [#assign class = "test-failure"]
            [@classCell cc="${indent}${test.name?html}"/]
        [#if test.isSuite()]
            [@contentCell cc="&nbsp;"/]
            [@contentCell cc="total: ${test.total}, errors: ${test.errors}, failures: ${test.failures}"/]
        [#else]
            [@classCell cc=test.status?lower_case/]
            [#if test.message?exists]
                [@openCell/]
                    <pre>${test.message?html}</pre>
                </td>
            [#else]
                [@contentCell cc="&nbsp;"/]
            [/#if]
        [/#if]
        </tr>
    [#if test.isSuite()]
            [#list test.children as child]
                [@showTestHTML test=child indent="${indent}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"/]
            [/#list]
        [/#if]
    [/#if]
[/#macro]

[#macro recipeFailedTestsHTML recipe]
    [#list recipe.commandResults as command]
        [#list command.artifacts as artifact]
            [#list artifact.children as fileArtifact]
                [#list fileArtifact.tests as test]
                    [@showTestHTML test=test/]
                [/#list]
            [/#list]
        [/#list]
    [/#list]
[/#macro]

[#macro recipeNodeFailedTestsHTML node]
    [@recipeFailedTestsHTML recipe=node.result/]
    [#list node.children as child]
        [@recipeNodeFailedTestsHTML node=child/]
    [/#list]
[/#macro]

[#macro buildFailedTestsHTML result]
    [#list result.root.children as child]
        [@recipeNodeFailedTestsHTML node=child/]
    [/#list]
[/#macro]

[#macro openCell type="td" class="content"]
    <${type} class="${class}" style="border: 1px solid #bbb; padding: 4px; text-align: left;">
[/#macro]

<#---------------------------------------------------------------------------
A content header cell
---------------------------------------------------------------------------->
[#macro contentHeader cc]
    [@openCell type="th"/]${cc}</th>
[/#macro]

<#---------------------------------------------------------------------------
A content cell
---------------------------------------------------------------------------->
[#macro contentCell cc]
    [@openCell/]${cc}</td>
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
[#macro linkCell cc url]
    [@openCell/]<a href="${url}">${cc}</a></td>
[/#macro]
