<#include "/tove/xhtml/controlheader.ftl" />

(function()
{
    var autoCreate = { tag: 'textarea', autocomplete: 'off' };

<#if parameters.cols?exists>
    fc.width = ${parameters.cols} * 7;
</#if>
<#if parameters.rows?exists>
    autoCreate.rows = ${parameters.rows};
</#if>
<#if parameters.wrap?exists>
    autoCreate.wrap = '${parameters.wrap}';
</#if>
<#if parameters.value?exists>
    fc.value = '${parameters.value?js_string}';
</#if>

    fc.autoCreate = autoCreate;
    form.add(new Ext.form.TextField(fc));
})();

<#include "/tove/xhtml/controlfooter.ftl" />