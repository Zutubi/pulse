<table>
    <#if table.rows?size &gt; 0>
        <#list table.rows as row>
        <tr>
            <#list row.cells as cell>
                <#if cell.parameters.type?exists>
                    <#if cell.parameters.type == "header">
                        <th colspan="${cell.span}">${cell.value?i18n}</th>
                    <#elseif cell.parameters.type == "action">
                        <td colspan="${cell.span}">
                        <#if cell.parameters.label == "edit">
                            <a href="edit">${cell.parameters.label?i18n}</a>
                        <#elseif cell.parameters.label == "delete">
                            <a href="delete.action?submit=delete&index=${row.index}&path=${path}&scope=${scope}">${cell.parameters.label?i18n}</a>
                        <#else>
                            ${cell.parameters.label?i18n}
                        </#if>
                        </td>
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