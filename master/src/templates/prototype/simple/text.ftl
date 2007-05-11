<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?html}';
</#if>
form.add(new Ext.form.TextField(fieldConfig));
