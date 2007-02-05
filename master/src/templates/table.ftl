<table>
    <tr>
        <th colspan="1">&nbsp;</th>
        <#list table.columns as column>
            <th colspan="${column.span}">${column.name?i18n}</th>
        </#list>
    </tr>
    <#if table.rows?size &gt; 0>
        <#list table.rows as row>
        <tr>
            <td colspan="1">${row.index}</td>
            <#list row.cells as cell>
                <#if cell.parameters.type?exists>
                    <td colspan="${cell.span}"><a href="">${cell.parameters.label?i18n}</a></td>
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