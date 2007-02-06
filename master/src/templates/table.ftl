<table>
    <#if table.rows?size &gt; 0>
        <#list table.rows as row>
        <tr>
            <#list row.cells as cell>
                <#if cell.parameters.type?exists>
                    <#if cell.parameters.type == "header">
                        <th colspan="${cell.span}">${cell.value?i18n}</th>
                    <#elseif cell.parameters.type == "action">
                        <td colspan="${cell.span}"><a href="">${cell.parameters.label?i18n}</a></td>
                    </#if>
                <#else>
                    <td colspan="${cell.span}">${cell.value}</td>
                </#if>
            </#list>
        </tr>
        </#list>
    <#else>
        <tr>
            <td>problem generating table.</td>
        </tr>
    </#if>
</table>