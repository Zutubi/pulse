<#-- render the wizard -->

<link rel="stylesheet" type="text/css" href="/css/prototype.css" media="screen"/>

<h2>${"label"?i18n}</h2>
<div style="color:gray">
    ${"introduction"?i18n}
</div>

<#-- TODO: review the css classes used here. -->
<#if wizard.decorate == true>
<table>
    <tr>
        <td valign="top" class="wizardsteps">
            <ul>
            <#list wizard.steps as step>
                <#assign index = step_index + 1/>
                <#assign labelkey = "${step}.label"/>
                <#assign stepclass = "wizardstep"/>
                <#if index == wizard.currentStep>
                    <#assign descrkey = "${step}.description"/>
                    <#assign stepclass = "currentwizardstep"/>
                </#if>
                <li class="${stepclass}"> ${index}: ${labelkey?i18n} </li>
            </#list>
            </ul>
        </td>
        <td valign="top">
            <p>${descrkey?i18n}</p>
            <#assign form = wizard.form/>
            <#include "form.ftl"/>
        </td>
    </tr>
</table>
<#else>
    <#assign form = wizard.form/>
    <#include "form.ftl"/>
</#if>
