[#ftl]
<html>
<head>
    <title>${project.name?html}: ${status}</title>
</head>
<body>
    <table cellspacing="0">
        [@summaryRow ch="Project" cc=project.name?html url=projectLink(result)/]        
[#if status == "healthy"]
    [#assign colour="#383"/]
[#else]
    [#assign colour="#b22"/]
[/#if]
        [@summaryRow ch="Status" cc=status colour=colour/]
        [@summaryRow ch="Latest Build" cc="${result.number?c} (${result.state.prettyString})" url=buildLink(result)/]
[#if !result.succeeded()]
    [#if lastHealthy?exists]
        [@summaryRow ch="Last Successful Build" cc="${lastHealthy.number?c} (${lastHealthy.stamps.prettyStartTime})" url=buildLink(lastHealthy)/]
    [/#if]
    [#if unsuccessfulBuilds?exists && unsuccessfulBuilds &gt; 0]
        [@summaryRow ch="Unsuccessful Builds" cc=unsuccessfulBuilds?c/]
    [/#if]
    [#if unsuccessfulDays?exists && unsuccessfulDays &gt; 0]
        [@summaryRow ch="Unsuccessful Days" cc=unsuccessfulDays?c/]
    [/#if]
[/#if]
    </table>
</body>
</html>
