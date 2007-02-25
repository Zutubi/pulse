<input type="submit"<#rt/>
<#if parameters.name?exists>
 name="${parameters.name?html}"<#rt/>
</#if>
<#if parameters.value?exists>
 value="${parameters.value?i18n}"<#rt/>
</#if>
<#if parameters.cssClass?exists>
 class="${parameters.cssClass?html}"<#rt/>
</#if>
<#if parameters.cssStyle?exists>
 style="${parameters.cssStyle?html}"<#rt/>
</#if>
<#if parameters.tabindex?exists>
 tabindex="${parameters.tabindex?html}"<#rt/>
</#if>
/>