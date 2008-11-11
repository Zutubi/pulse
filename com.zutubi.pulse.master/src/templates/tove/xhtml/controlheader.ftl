<#if parameters.label?exists>
    fc.fieldLabel = '${parameters.label?i18n?js_string}';
</#if>
<#if parameters.id?exists>
    fc.id = '${parameters.id?js_string}';
</#if>
<#if parameters.name?exists>
    fc.name = '${parameters.name?js_string}';
</#if>
<#if parameters.noOverride?exists>
    fc.readOnly = true;
</#if>
    fc.submitOnEnter = ${parameters.submitOnEnter?default(true)?string};
