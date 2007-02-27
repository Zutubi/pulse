<#-- render the wizard-->

<table>
    <tr>
        <td valign="top">
            <ul>
            <#list 1..wizard.stepCount as x>
                <li><#if x == wizard.currentStep><b>step</b><#else>step</#if> ${x}</li>
            </#list>
            </ul>
        </td>
        <td>
            <#include "form.ftl"/>
        </td>
    </tr>
</table>

