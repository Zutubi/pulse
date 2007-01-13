<input type="checkbox"<#rt/>
<#if parameters.name?exists>
 name="${parameters.name?html}"<#rt/>
</#if>
<#if parameters.fieldValue?exists>
 value="${parameters.fieldValue?html}"<#rt/>
</#if>
<#if parameters.value?exists && parameters.fieldValue?exists && parameters.value == parameters.fieldValue>
 checked="checked"<#rt/>
</#if>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
<#if parameters.readonly?exists>
 readonly="readonly"<#rt/>
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
<#include "/forms/simple/scripting-events.ftl" />
/>