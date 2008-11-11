<#include "/tove/xhtml/controlheader.ftl" />

<#if parameters.value?exists>
    fc.value = '${parameters.value?js_string}';
</#if>
fc.autoCreate = {tag: 'input', type: 'file', autocomplete: 'off'};
<#if parameters.size?exists>
    fc.autoCreate.size = ${parameters.size};
</#if>
fc.inputType = 'file';
fc.width = 'auto';

${form.name}.add(new Ext.form.TextField(fc));

<#include "/tove/xhtml/controlfooter.ftl" />