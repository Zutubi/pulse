<#include "/tove/xhtml/controlheader.ftl" />

fc.autoCreate = {tag: "input", type: "text", size: "20"};
<#if parameters.value?exists>
    fc.value = '${parameters.value?js_string}';
</#if>
<#if parameters.size?exists>
    fc.width = ${parameters.size};
</#if>
${form.name}.add(new Ext.form.TextField(fc));

<#include "/tove/xhtml/controlfooter.ftl" />