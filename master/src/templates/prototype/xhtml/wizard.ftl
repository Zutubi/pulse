<#-- render the wizard -->

<link rel="stylesheet" type="text/css" href="/css/prototype.css" media="screen"/>

<#-- TODO: review the css classes used here. -->
<#if decorate == true>
<table>
    <tr>
        <td valign="top" class="wizardsteps">
            <ul>
            <#list 1..wizard.stepCount as x>
                <#assign xlabel>${x}.label</#assign>
                <#assign stepclass>wizardstep</#assign>
                <#if x == wizard.currentStep>
                    <#assign xdescr>${x}.description</#assign>
                    <#assign stepclass>currentwizardstep</#assign>
                </#if>
                <li class="<#if x == wizard.currentStep>currentwizardstep<#else>wizardstep</#if>"> ${x}: ${xlabel?i18n} </li>
            </#list>
            </ul>
        </td>
        <td valign="top">
            <p>${xdescr?i18n}</p>
            <#include "form.ftl"/>
        </td>
    </tr>
</table>
<#else>
    <#include "form.ftl"/>
</#if>

