<#include "/tove/xhtml/controlheader.ftl" />

<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?js_string}';
</#if>
<#if parameters.size?exists>
    fieldConfig.width = ${parameters.size};
</#if>
form.add(new Ext.form.TextField(fieldConfig));

<#include "/tove/xhtml/controlfooter.ftl" />