<table>
    <#if table.rows?size &gt; 0>
        <#list table.rows as row>
        <tr>
            <#list row.cells as cell>
                <#if cell.parameters.type?exists && cell.parameters.type == "header">
                    <th class="content" colspan="${cell.span}"><#if cell.value?exists>${cell.value?i18n}</#if></th>
                <#else>
                    <td class="content" colspan="${cell.span}">
                        <#if cell.link?exists || cell.onclick?exists>
                            <a
                            <#if cell.link?exists>
                                href="${base}/${cell.link}"
                            <#else>
                                href="#"
                            </#if>
                            <#if cell.onclick?exists>
                                <#if cell.parameters.key?exists>
                                    onclick="${cell.onclick}('${path}/${cell.parameters.key}'); return false;"
                                <#else>
                                    onclick="${cell.onclick}('${path}'); return false;"
                                </#if>
                            </#if>
                            >
                        </#if>
                        <#if cell.value?exists>${cell.value}</#if>
                        <#if cell.link?exists || cell.onclick?exists>
                            </a>
                        </#if>
                    </td>
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
