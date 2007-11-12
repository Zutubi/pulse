<h3 class="config-header">${"label"?i18n}</h3>
<div class="config-form-container">
    <p class="introduction">
        ${"introduction"?i18n}
    </p>
<#if wizard.decorate>
    <table class="wizard">
        <tr>
    <#list wizard.steps as step>
        <#assign index = step_index + 1/>
        <#assign labelkey = "wizard.step.${step.name}.label"/>
        <#assign stepclass = "wizardstep"/>
        <#if step.id == wizard.currentStep>
            <#assign descrkey = "wizard.step.${step.name}.description"/>
            <#assign stepclass = "wizardstep currentstep"/>
        </#if>
            <td valign="top" class="${stepclass}">
                ${i18nText(step.type, labelkey)}
            </td>
    </#list>
        </tr>
</#if>
        <tr>
            <td colspan="${wizard.steps?size}" class="wizardcontent">
<#if wizard.decorate>
                <p>${descrkey?i18n}</p>
</#if>
                <#assign form = wizard.form/>
                <#include "form.ftl"/>
            </td>
        </tr>
    </table>
</div>
