<#include "/tove/xhtml/controlheader.ftl" />

<#if parameters.value?exists>
    fc.value = '${parameters.value?js_string}';
</#if>
<#if parameters.size?exists>
    fc.width = ${parameters.size};
</#if>
form.add(new Ext.form.TextField(fc));

<#include "/tove/xhtml/controlfooter.ftl" />