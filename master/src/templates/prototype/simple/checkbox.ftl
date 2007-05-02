<input type="checkbox"<#rt/>
 name="${parameters.name?html}"<#rt/>
 value="true"<#rt/>
<#if parameters.value?exists && parameters.value == "true">
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
></input>
<input type="hidden"<#rt/>
 name="${parameters.name?html}.default"<#rt/>
 value="false"<#rt/>
></input>