<#if field.label?exists>
    fc.fieldLabel = '${field.label?i18n?js_string}';
</#if>
<#if field.id?exists>
    fc.id = '${field.id?js_string}';
</#if>
<#if field.name?exists>
    fc.name = '${field.name?js_string}';
</#if>
<#if parameters.noOverride?exists>
    fc.readOnly = true;
</#if>
    fc.submitOnEnter = ${parameters.submitOnEnter?default(true)?string};
