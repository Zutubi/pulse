[#ftl]
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
    <title>project :: ${result.project.name?html} :: build ${result.number?c}</title>
    <style type="text/css">
    <!--
body {
    font: sans-serif;
}

h1 {
    font-size: 150%;
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
</head>
<body>
    <h1 style="font-size: 150%">
        project ::
        <a href="http://${hostname}/currentBuild.action?id=${result.project.id?c}">${result.project.name?html}</a> ::
        <a href="http://${hostname}/viewBuild.action?id=${result.id?c}">build ${result.number?c}</a>
    </h1>
<table>
<tr><td>
    <table class="content" style="border-collapse: collapse; border: 1px solid #bbb; margin-bottom: 16px;">
        <tr>
            <th class="heading" colspan="11" style="border: 1px solid #bbb; padding: 4px; text-align: left; vertical-align: top; background: #e9e9f5;">
                summary
            </th>
        </tr>
        <tr>
            [@contentHeader cc="id"/]
            [@contentHeader cc="status"/]
            [@contentHeader cc="spec"/]
            [@contentHeader cc="reason"/]
            [@contentHeader cc="when"/]
            [@contentHeader cc="elapsed"/]
            <th class="content" colspan="3" style="border: 1px solid #bbb; padding: 4px; text-align: left;">actions</th>
        </tr>
        <tr>
            [#assign class = result.state.string]
            [@classCell cc=result.number?c/]
            [@classCell cc=result.stateName?lower_case/]
            [@classCell cc=result.buildSpecification/]
            [@classCell cc=result.reason.summary/]
            [@classCell cc=result.stamps.prettyStartTime/]
            [@classCell cc=result.stamps.prettyElapsed/]
            [@linkCell cc="view" url="http://${hostname}/viewBuild.action?id=${result.id?c}"/]
            [@linkCell cc="artifacts" url="http://${hostname}/viewBuildArtifacts.action?id=${result.id?c}"/]
            [@linkCell cc="tests" url="http://${hostname}/viewTests.action?id=${result.id?c}"/]
        </tr>
    </table>
</td></tr>
<tr><td>
    <table class="content" style="border-collapse: collapse; border: 1px solid #bbb; margin-bottom: 16px;">
        <tr>
            <th class="heading" colspan="7" style="border: 1px solid #bbb; padding: 4px; text-align: left; vertical-align: top; background: #e9e9f5;">
                stages
            </th>
        </tr>
        <tr>
            [@contentHeader cc="stage"/]
            [@contentHeader cc="recipe"/]
            [@contentHeader cc="host"/]
            [@contentHeader cc="status"/]
            [@contentHeader cc="when"/]
            [@contentHeader cc="elapsed"/]
            <th class="content" colspan="1" style="border: 1px solid #bbb; padding: 4px; text-align: left;">actions</th>
        </tr>
[#list result.root.children as child]
        <tr>
            [#assign class = child.result.state.string]
            [@classCell cc=child.stage/]
            [@classCell cc=child.result.recipeNameSafe/]
            [@classCell cc=child.hostSafe/]
            [@classCell cc=child.result.stateName?lower_case/]
            [@classCell cc=child.result.stamps.prettyStartTime/]
            [@classCell cc=child.result.stamps.prettyElapsed/]
            [@linkCell cc="view" url="http://${hostname}/commandLog.action?id=${result.id?c}&amp;selectedNode=${child.id?c}"/]
        </tr>
[/#list]
    </table>
</td></tr>
[#if result.scmDetails?exists]
    [#assign changes = result.scmDetails.changelists]
<tr><td>
    <table class="content" style="border-collapse: collapse; border: 1px solid #bbb; margin-bottom: 16px;">
        <th class="heading" colspan="5" style="border: 1px solid #bbb; padding: 4px; text-align: left; vertical-align: top; background: #e9e9f5;">
            changes
        </th>
        <tr>
            [@contentHeader cc="revision"/]
            [@contentHeader cc="who"/]
            [@contentHeader cc="when"/]
            [@contentHeader cc="comment"/]
            [@contentHeader cc="actions"/]
        </tr>
    [#if changes?size &gt; 0]
        [#list changes as change]
        <tr>
            [@dynamicCell cc=change.revision.revisionString/]
            [@dynamicCell cc=change.user/]
            [@dynamicCell cc=change.prettyTime/]
            [@dynamicCell cc=renderer.trimmedString(change.comment, 60)/]
            [@linkCell cc="view" url="http://${hostname}/viewChangelist.action?id=${change.id?c}&amp;buildId=${result.id?c}"/]
        </tr>
        [/#list]
    [#else]
        <tr>
            <td class="content" colspan="5" style="border: 1px solid #bbb; padding: 4px; text-align: left;">
                no changes in this build
            </td>
        <tr>
    [/#if]
    </table>
</td></tr>
[/#if]

[#if result.hasMessages(errorLevel) || result.hasMessages(warningLevel)]
<tr><td>
    <table class="content" style="border-collapse: collapse; border: 1px solid #bbb; margin-bottom: 16px;">
        <tr>
        <th class="heading" style="border: 1px solid #bbb; padding: 4px; text-align: left; vertical-align: top; background: #e9e9f5;">
            features
        </th>
        <tr>
        <tr>
            <td class="content" style="border: 1px solid #bbb; padding: 4px; text-align: left;">
                [@buildMessagesHTML result=result level=errorLevel/]
                [@buildMessagesHTML result=result level=warningLevel/]
            </td>
        </tr>
    </table>
</td></tr>
[/#if]

[#assign testSummary = result.testSummary]
[#if !testSummary.allPassed()]
<tr><td>
    <table class="content" style="border-collapse: collapse; border: 1px solid #bbb; margin-bottom: 16px;">
        <th class="heading" colspan="3" style="border: 1px solid #bbb; padding: 4px; text-align: left; vertical-align: top; background: #e9e9f5;">
            broken tests (total: ${testSummary.total}, errors: ${testSummary.errors}, failures: ${testSummary.failures})
        </th>
        <tr>
            [@contentHeader cc="test"/]
            [@contentHeader cc="status"/]
            [@contentHeader cc="details"/]
        </tr>
        [@buildFailedTestsHTML result=result/]
    </table>
</td></tr>
[/#if]
</table>
</body>
</html>
