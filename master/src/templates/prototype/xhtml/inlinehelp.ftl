<#assign helpkey>${parameters.name}.help</#assign>
<#assign helpmsg>${helpkey?i18n}</#assign>
<#if helpmsg?exists && helpkey != helpmsg>
<tr>
    <td>&nbsp;</td>
    <td class="inline-help">${helpmsg}</td>
</tr>
</#if>