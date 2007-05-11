<#if parameters.label?exists>
    fieldConfig.fieldLabel = '${parameters.label?i18n}';
</#if>
<#if parameters.id?exists>
    fieldConfig.id = '${parameters.id?html}';
</#if>
<#if parameters.required?default(false)>
    fieldConfig.allowBlank = false;
</#if>
