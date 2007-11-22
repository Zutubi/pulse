<#include "/prototype/xhtml/controlheader.ftl" />

<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?js_string}';
</#if>
fieldConfig.autoCreate = {tag: 'input', type: 'file', autocomplete: 'off'};
<#if parameters.size?exists>
    fieldConfig.autoCreate.size = ${parameters.size};
</#if>
fieldConfig.inputType = 'file';
fieldConfig.width = 'auto';

form.add(new Ext.form.TextField(fieldConfig));

<#include "/prototype/xhtml/controlfooter.ftl" />