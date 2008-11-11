<#include "/tove/xhtml/controlheader.ftl" />

<#if parameters.value?exists>
    fc.value = '${parameters.value?js_string}';
</#if>
${form.name}.add(new Ext.form.Hidden(fc));

<#-- No footer for hidden fields -->
