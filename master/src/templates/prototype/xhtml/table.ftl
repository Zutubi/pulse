<table id="config.table">
<#assign tablewidth = table.columns?size + 1/>
    <tr>
        <th class="heading" colspan="${tablewidth}">${table.heading?html}</th>
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
<#assign firstaction = true/>
<#list table.getActions(item) as action>
    <#assign actionlabel = "${action}.label"/>
    <#if action == "view">
        <#assign suffix = ""/>
    <#elseif action == "delete">
        <#assign suffix = "?${action}=confirm"/>
    <#else>
        <#assign suffix = "?${action}"/>
    <#/if>
        |
    <a href="${base}/config/${item.configurationPath}${suffix}">${actionlabel?i18n}</a>
    <assign firstaction = false/>
</#list>
        </td>
    </tr>
</#list>
<#else>
    <tr>
        <td class="content" colspan="${tablewidth}">${"no.data.available"?i18n}</td>
    </tr>
</#if>
<#if table.addAllowed>
    <tr>
        <td class="content" colspan="${tablewidth}"><a href="${base}/config/${path}?wizard">${"add.label"?i18n}</a></td>
    </tr>
</#if>
</table>