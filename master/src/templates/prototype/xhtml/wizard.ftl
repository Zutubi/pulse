<h2>${"label"?i18n}</h2>
<p class="introduction">
    ${"introduction"?i18n}
</p>

<#if wizard.decorate == true>
<table>
    <tr>
        <td valign="top" class="wizardsteps">
            <ul>
            <#list wizard.steps as step>
                <#assign index = step_index + 1/>
                <#assign labelkey = "wizard.step.${step.name}.label"/>
                <#assign stepclass = "wizardstep"/>
                <#if step.id == wizard.currentStep>
                    <#assign descrkey = "wizard.step.${step.name}.description"/>
                    <#assign stepclass = "currentwizardstep"/>
                </#if>
                <li class="${stepclass}"> ${index}: ${i18nText(step.type, labelkey)} </li>
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
