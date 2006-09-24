<select<#rt/>
 name="${parameters.name?default("")?html}"<#rt/>
<#if parameters.size?exists>
 size="${parameters.size?html}"<#rt/>
</#if>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
<#if parameters.tabindex?exists>
 tabindex="${parameters.tabindex?html}"<#rt/>
</#if>
<#if parameters.id?exists>
 id="${parameters.id?html}"<#rt/>
</#if>
<#if parameters.cssClass?exists>
 class="${parameters.cssClass?html}"<#rt/>
</#if>
<#if parameters.cssStyle?exists>
 style="${parameters.cssStyle?html}"<#rt/>
</#if>
<#if parameters.multiple?exists>
 multiple="multiple"<#rt/>
</#if>
<#include "/forms/simple/scripting-events.ftl" />
>
<#if parameters.headerKey?exists && parameters.headerValue?exists>
    <option value="${parameters.headerKey?html}">${parameters.headerValue?html}</option>
</#if>
<#if parameters.emptyOption?default(false)>
    <option value=""></option>
</#if>
<#list parameters.list as item>
    <#assign optionValue = item/>
    <#if parameters.listValues[item]?exists>
        <#assign optionLabel = parameters.listValues[item]/>
    <#else>
        <#assign optionLabel = item/>
    </#if>
    <option value="${optionValue?html}"<#rt/>
        <#if parameters.value?exists && parameters.value == optionValue>
 selected="selected"<#rt/>
        </#if>
    ><@i18n>${optionLabel?html}</@i18n></option><#lt/>
</#list>
</select>