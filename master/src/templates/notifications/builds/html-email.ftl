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
</head>
<body>
    <h1 style="font-size: 130%">
        project ::
        <a href="${projectLink(result)}">${result.project.name?html}</a> ::
        <a href="${buildLink(result)}">build ${result.number?c}</a>
    </h1>
<table>
<tr><td>
    [@buildSummaryHTML result=result/]
</td></tr>
<tr><td>
    [@buildStageSummariesHTML result=result/]
</td></tr>
[#if changelists?exists]
<tr><td>
    [@buildChangesHTML changelists=changelists/]
</td></tr>
[/#if]

[#if result.hasMessages(errorLevel) || result.hasMessages(warningLevel)]
<tr><td>
    [@openTable/]
        [@headingRow heading="features" span=1/]
        <tr>
            [@openCell/]
                [@buildMessagesHTML result=result level=errorLevel/]
                [@buildMessagesHTML result=result level=warningLevel/]
            </td>
        </tr>
    </table>
</td></tr>
[/#if]

[#assign testSummary = result.testSummary]
[#if !testSummary.allPassed()]
    [@buildFailedTestsHTML result=result/]
[/#if]
</table>
</body>
</html>
