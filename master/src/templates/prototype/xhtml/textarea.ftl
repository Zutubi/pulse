<#include "/prototype/xhtml/controlheader.ftl" />

(function()
{
    var autoCreate = { tag: 'textarea', autocomplete: 'off' };

<#if parameters.cols?exists>
    fieldConfig.width = ${parameters.cols} * 7;
</#if>
<#if parameters.rows?exists>
    autoCreate.rows = ${parameters.rows};
</#if>
<#if parameters.wrap?exists>
    autoCreate.wrap = '${parameters.wrap}';
</#if>
<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?js_string}';
</#if>

    fieldConfig.autoCreate = autoCreate;
    form.add(new Ext.form.TextField(fieldConfig));
})();

<#include "/prototype/xhtml/controlfooter.ftl" />