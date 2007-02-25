<#--
Only show message if errors are available. 
-->
<#--
<#assign hasFieldErrors = fieldErrors?exists && fieldErrors[parameters.name]?exists/>
<#if hasFieldErrors>
<#list fieldErrors[parameters.name] as error>
<tr errorFor="${parameters.id}">
<#if parameters.labelposition?default("") == 'top'>
   <td align="left" valign="top" colspan="2"><#rt/>
<#else>
   <td align="center" valign="top" colspan="2"><#rt/>
</#if>
       <span class="errorMessage">${error?html}</span><#t/>
   </td><#lt/>
</tr>
</#list>
</#if>
-->

<tr>
   <td align="right" valign="top"><#rt/>
<#if parameters.label?exists>
   <label <#t/>
<#if parameters.id?exists>
 for="${parameters.id?html}" <#t/>
</#if>
 class="label"<#t/>
><#t/>
<#if parameters.required?default(false)>
       <span class="required">*</span><#t/>
</#if>
 ${parameters.label?i18n}:</label><#t/>
</#if>
   </td><#lt/>
