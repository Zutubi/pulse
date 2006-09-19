<#list parameters.list as item>
<#assign itemKey = item/>
<#assign itemValue = item/>
<input type="radio" name="${parameters.name?html}" id="${parameters.name?html}${itemKey?html}"<#rt/>
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
<label for="${parameters.name?html}${itemKey?html}"><#rt/>
    ${itemValue}<#t/>
</label>
</#list>
