<table id="config-table">
<#assign tablewidth = table.columns?size + 1/>
    <tr>
        <th class="header" colspan="${tablewidth}">${table.heading?html}</th>
    </tr>
    <tr>
<#list table.columns as column>
<#assign header>${column.name}.label</#assign>
        <th colspan="1">${header?i18n}</th>
</#list>
        <th colspan="1">${"actions.label"?i18n}</th>
    </tr>
<#if data?exists && data?size &gt; 0>
<#list data as item>
    <tr>
<#list table.columns as column>
    <#assign value = column.getValue(item)/>
        <td>${value?string}</td>
</#list>
        <td>
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
    <a href="${base}/config/${item.configurationPath}${suffix}"><#if ${action?icon?exists}><img alt="${actionlabel?i18n}" src="${base}/images/config/actions/${action.icon}.gif"/> </#if>${actionlabel?i18n}</a>
    <assign firstaction = false/>
</#list>
        </td>
    </tr>
</#list>
<#else>
    <tr>
        <td colspan="${tablewidth}">${"no.data.available"?i18n}</td>
    </tr>
</#if>
<#if table.addAllowed>
    <tr>
        <td colspan="${tablewidth}"><a href="${base}/config/${path}?wizard">${"add.label"?i18n}</a></td>
    </tr>
</#if>
</table>