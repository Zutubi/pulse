<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?html}';
</#if>
fieldConfig.inputType = 'password';
form.add(new Ext.form.TextField(fieldConfig));
