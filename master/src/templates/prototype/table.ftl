<table>
    <#if table.rows?size &gt; 0>
        <#list table.rows as row>
        <tr>
            <#list row.cells as cell>
                <#if cell.parameters.type?exists>
                    <#if cell.parameters.type == "header">
                        <th colspan="${cell.span}"><#if cell.value?exists>${cell.value?i18n}</#if></th>
                    <#elseif cell.parameters.type == "action">
                        <td colspan="${cell.span}">
                        <#if cell.parameters.label == "edit">
                            &nbsp;<a href="configuration.action?path=${path}/${cell.parameters.key}">${cell.parameters.label?i18n}</a>
                        <#elseif cell.parameters.label == "delete">
                            &nbsp;<a href="reset.action?submit=delete&path=${path}/${cell.parameters.key}">${cell.parameters.label?i18n}</a>
                        <#else>
                            &nbsp;${cell.parameters.label?i18n}
                        </#if>
                        </td>
                    </#if>
                <#else>
                    <td colspan="${cell.span}"><#if cell.value?exists>${cell.value}</#if></td>
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