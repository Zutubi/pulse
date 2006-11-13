<#--
Only show message if errors are available.
This will be done if ActionSupport is used.
-->
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

<#--
if the label position is top,
then give the label it's own row in the table
-->
<tr>
<#if parameters.labelposition?default("") == 'top'>
   <td align="left" valign="top" colspan="2"><#rt/>
<#else>
   <td align="right" valign="top"><#rt/>
</#if>
<#if parameters.label?exists>
   <label <#t/>
<#if parameters.id?exists>
       for="${parameters.id?html}" <#t/>
</#if>
<#if hasFieldErrors>
       class="errorLabel"<#t/>
<#else>
       class="label"<#t/>
</#if>
   ><#t/>
<#if parameters.required?default(false)>
       <span class="required">*</span><#t/>
</#if>
       ${parameters.label?html}:</label><#t/>
</#if>
   </td><#lt/>
<#-- add the extra row -->
<#if parameters.labelposition?default("") == 'top'>
</tr>
<tr>
</#if>
