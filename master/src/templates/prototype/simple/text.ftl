<input type="text"<#rt/>
 name="${parameters.name?default("")?html}"<#rt/>
<#if parameters.id?exists>
 id="${parameters.id?html}"<#rt/>
</#if>
<#if parameters.size?exists>
 size="${parameters.size?html}"<#rt/>
</#if>
<#if parameters.maxlength?exists>
 maxlength="${parameters.maxlength?html}"<#rt/>
</#if>
<#if parameters.value?exists>
 value="${parameters.value?html}"<#rt/>
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
<#if parameters.cssClass?exists>
 class="${parameters.cssClass?html}"<#rt/>
</#if>
<#if parameters.cssStyle?exists>
 style="${parameters.cssStyle?html}"<#rt/>
</#if>
/>