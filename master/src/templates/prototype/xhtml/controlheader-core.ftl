<#--
Only show message if errors are available. 
-->
<#assign hasFieldErrors = fieldErrors?exists && fieldErrors[parameters.name]?exists/>
<#if hasFieldErrors>
<#list fieldErrors[parameters.name] as error>
<tr errorFor="${parameters.id}">
    <th class="error-message" colspan="2"><#rt/>
        ${error?i18n}<#t/>
    </th><#lt/>
</tr>
</#list>
</#if>

<tr>
   <th align="right" valign="top" <#rt/> 
<#if hasFieldErrors>
 class="error-label"<#t/>
<#else>
 class="label"<#t/>
</#if>
><#rt/>
<#if parameters.label?exists>
   <label <#t/>
<#if parameters.id?exists>
 for="${parameters.id?html}" <#t/>
</#if>
<#if hasFieldErrors>
 class="error-label"<#t/>
<#else>
 class="label"<#t/>
</#if>
><#t/>
 ${parameters.label?i18n} <#t/>
<#if parameters.required?default(false)>
       <span class="required">*</span><#t/>
</#if>
 :</label><#t/>
</#if>
   </th><#lt/>
