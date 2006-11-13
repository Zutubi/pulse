<#list parameters.list as item>
    <#if parameters.listKey?exists>
        <#assign itemKey = item[parameters.listKey]/>
    <#else>
        <#assign itemKey = item/>
    </#if>
    <#if parameters.listValue?exists>
        <#assign itemValue = item[parameters.listValue]/>
    <#else>
        <#assign itemValue = item/>
    </#if>
<input type="radio"<#rt/>
<#if parameters.name?exists>
 name="${parameters.name?html}" id="${parameters.name?html}${itemKey?html}"<#rt/>
</#if>
<#if parameters.value?exists && parameters.value == itemKey>
 checked="checked"<#rt/>
</#if>
<#if itemKey?exists>
 value="${itemKey?html}"<#rt/>
</#if>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
<#if parameters.tabindex?exists>
 tabindex="${parameters.tabindex?html}"<#rt/>
</#if>
<#if parameters.cssClass?exists>
 class="${parameters.cssClass?html}"<#rt/>
</#if>
<#if parameters.cssStyle?exists>
 style="${parameters.cssStyle?html}"<#rt/>
</#if>
<#include "/forms/simple/scripting-events.ftl" />
/><#rt/>
<label<#rt/>
<#if parameters.name?exists>
 for="${parameters.name?html}${itemKey?html}"<#rt/>
</#if>
><#rt/>
    ${itemValue}<#t/>
</label>
</#list>
