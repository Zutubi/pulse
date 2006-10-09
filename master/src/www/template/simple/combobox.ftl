<#include "/${parameters.templateDir}/simple/text.ftl" />
<br/>
<#if parameters.list?exists>
<select onChange="this.form.elements['${parameters.name?html}'].value=this.options[this.selectedIndex].value"<#rt/>
    <#if parameters.id?exists>
 id="${parameters.id?html}_select"<#rt/>
    </#if>
    <#if parameters.disabled?exists && parameters.disabled>
 disabled="disabled"<#rt/>
    </#if>
>
    <@ww.iterator value="parameters.list">
        <#if parameters.listKey?exists>
            <#assign itemKey = stack.findValue(parameters.listKey)/>
        <#else>
            <#assign itemKey = stack.findValue('top')/>
        </#if>
        <#assign itemKeyStr = itemKey.toString() />
        <#if parameters.listValue?exists>
            <#assign itemValue = stack.findString(parameters.listValue)/>
        <#else>
            <#assign itemValue = stack.findString('top')/>
        </#if>
    <option value="${itemKeyStr?html}"<#rt/>
        <#if parameters.nameValue == itemKeyStr>
 selected="selected"<#rt/>
        </#if>
    >${itemValue?html}</option><#lt/>
    </@ww.iterator>
</select>
</#if>