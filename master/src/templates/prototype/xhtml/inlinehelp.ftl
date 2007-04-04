<#assign hint>${parameters.name}.hint</#assign>
<#assign inlinehelp>${hint?i18n}</#assign>
<#if inlinehelp?exists && hint != inlinehelp>
<tr>
    <td>&nbsp;</td>
    <td class="inline-help">${inlinehelp}</td>
</tr>
</#if>