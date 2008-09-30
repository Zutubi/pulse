<#if parameters.label?exists>
    fieldConfig.fieldLabel = '${parameters.label?i18n?js_string}';
</#if>
<#if parameters.id?exists>
    fieldConfig.id = '${parameters.id?js_string}';
</#if>
<#if parameters.name?exists>
    fieldConfig.name = '${parameters.name?js_string}';
</#if>
<#if parameters.noOverride?exists>
    fieldConfig.readOnly = true;
</#if>

