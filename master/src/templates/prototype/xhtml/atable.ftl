<table>
<#assign tablewidth = table.columns?size + 1/>
    <tr>
        <th class="heading" colspan="${tablewidth}">${"table.header.label"?i18n}</th>
    </tr>
    <tr>
<#list table.columns as column>
<#assign header>${column.name}.label</#assign>
        <th class="content" colspan="1">${header?i18n}</th>
</#list>
        <th class="content" colspan="1">${"actions.label"?i18n}</th>
    </tr>
<#if data?exists && data?size &gt; 0>
<#list data as item>
    <tr>
<#list table.columns as column>
    <#assign value = column.getValue(item)/>
        <td class="content">${value?string}</td>
</#list>
        <td class="content">
<#list table.getActions(item) as action>
    <#assign actionlabel>${action}.label</#assign>
<#if action == "edit">
        <a onclick="selectPath('${item.configurationPath}'); return false;">${"edit.label"?i18n}</a>
<#elseif action == "delete">
        <a onclick="deletePath('${item.configurationPath}'); return false;">${"delete.label"?i18n}</a>
<#else>
        <a onclick="actionPath('${item.configurationPath}?${action}'); return false;">${actionlabel?i18n}</a>
</#if>
</#list>
        </td>
    </tr>
</#list>
<#else>
    <tr>
        <td class="content" colspan="${tablewidth}">${"no.data.available"?i18n}</td>
    </tr>
</#if>
    <tr>
        <td class="content" colspan="${tablewidth}"><a onclick="addToPath('${path}'); return false;">${"add.label"?i18n}</a></td>
    </tr>
</table>