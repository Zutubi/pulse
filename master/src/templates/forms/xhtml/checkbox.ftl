<#assign hasFieldErrors = fieldErrors?exists && fieldErrors[parameters.name]?exists/>
<#if hasFieldErrors>
<#list fieldErrors[parameters.name] as error>
<tr<#rt/>
<#if parameters.id?exists>
 errorFor="${parameters.id}"<#rt/>
</#if>
>
    <td align="left" valign="top" colspan="2"><#rt/>
        <span class="errorMessage">${error?html}</span><#t/>
    </td><#lt/>
</tr>
</#list>
</#if>
<tr>
    <td valign="top" colspan="2">
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="middle"><#include "/forms/simple/checkbox.ftl" /></td>
                <td width="100%" valign="middle"><#rt/>
                    <label<#t/>
<#if parameters.id?exists>
 for="${parameters.id?html}"<#rt/>
</#if>
<#if hasFieldErrors>
 class="checkboxErrorLabel"<#rt/>
<#else>
 class="checkboxLabel"<#rt/>
</#if>
><#rt/>
<#if parameters.label?exists>
 ${parameters.label?html}</label><#rt/>
</#if>
                </td><#lt/>
            </tr>
        </table>
    <#include "/forms/xhtml/controlfooter.ftl" /><#nt/>