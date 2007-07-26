[#ftl]
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
    <title>project :: ${result.project.name?html} :: build ${result.number?c}</title>
    [@stylesHTML/]
</head>
<body>
    <h1 style="font-size: 130%">
        project ::
        <a href="${projectLink(result)}">${result.project.name?html}</a> ::
        <a href="${buildLink(result)}">build ${result.number?c}</a>
    </h1>
    [@buildLinksHTML result=result/]
    [@stageLogLinksHTML result=result/]
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
