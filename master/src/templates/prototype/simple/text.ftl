<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?js_string}';
</#if>
form.add(new Ext.form.TextField(fieldConfig));
