<#assign hint>${parameters.name}.hint</#assign>
<#if hint?i18n?exists && hint != hint?i18n>
<tr>
    <td>&nbsp;</td>
    <td class="inline-help">${hint?i18n}</td>
</tr>
</#if>