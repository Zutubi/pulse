(function()
{
    var autoCreate = { tag: 'textarea', autocomplete: 'off' };
    
<#if parameters.cols?exists>
    autoCreate.cols: ${parameters.cols};
</#if>
<#if parameters.rows?exists>
    autoCreate.rows: ${parameters.rows};
</#if>
<#if parameters.wrap?exists>
    autoCreate.wrap: '${parameters.wrap}';
</#if>

    fieldConfig.autoCreate = autoCreate;
    form.add(new Ext.form.TextField(fieldConfig));
})();
