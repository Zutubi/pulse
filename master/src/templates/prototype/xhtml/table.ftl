<table>
    <#if table.rows?size &gt; 0>
        <#list table.rows as row>
        <tr>
            <#list row.cells as cell>
                <#if cell.parameters.type?exists>
                    <#if cell.parameters.type == "header">
                        <th class="content" colspan="${cell.span}"><#if cell.value?exists>${cell.value?i18n}</#if></th>
                    <#elseif cell.parameters.type == "action">
                        <td  class="content" colspan="${cell.span}">
                        <#if cell.parameters.label == "edit">
                            &nbsp;<a href="configuration.action?path=${path}/${cell.parameters.key}">${cell.parameters.label?i18n}</a>
                        <#elseif cell.parameters.label == "delete">
                            &nbsp;<a href="delete.action?submitField=delete&path=${path}/${cell.parameters.key}">${cell.parameters.label?i18n}</a>
                        <#elseif cell.parameters.label == "add">
                            &nbsp;<a href="${action}">${cell.parameters.label?i18n}</a>
                        <#else>
                            &nbsp;${cell.parameters.label?i18n}
                        </#if>
                        </td>
                    </#if>
                <#else>
                    <td class="content" colspan="${cell.span}"><#if cell.value?exists>${cell.value}</#if></td>
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