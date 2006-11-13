<textarea<#rt/>
 name="${parameters.name?default("")?html}"<#rt/>
<#if parameters.cols?exists>
 cols="${parameters.cols?default("")?html}"<#rt/>
</#if>
<#if parameters.rows?exists>
 rows="${parameters.rows?default("")?html}"<#rt/>
</#if>
<#if parameters.wrap?exists>
 wrap="${parameters.wrap?html}"<#rt/>
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
><#rt/>
<#if parameters.value?exists>
${parameters.value}<#t/>
</#if>
</textarea>