<table>
    <tr>
        <#list table.columns as column>
            <th>${column.name}</th>
        </#list>
    </tr>
    <#if table.rows.size &gt; 0>
        <#list table.rows as row>
        <tr>
            <#list row.columns as column>
                <td span="1">${column.value}</td>
            </#list>
        </tr>
        </#list>
    <#else>
        <tr>
            <td>no data duuude</td>
        </tr>
    </#if>
</table>