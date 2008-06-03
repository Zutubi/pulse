<#include "/prototype/xhtml/controlheader.ftl" />

<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?js_string}';
</#if>
form.add(new Ext.form.Hidden(fieldConfig));

<#include "/prototype/xhtml/controlfooter.ftl" />
