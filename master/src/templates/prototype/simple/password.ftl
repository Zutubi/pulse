<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?js_string}';
</#if>
fieldConfig.inputType = 'password';
form.add(new Ext.form.TextField(fieldConfig));
