<#include "/prototype/xhtml/controlheader.ftl" />

(function()
{
    var data = [];

    <#list parameters.value as item>
        data.push(['${item?js_string}']);
    </#list>

    var store = new Ext.data.SimpleStore({
        fields: ['value'],
        data: data
    });

    fieldConfig.store = store;
    fieldConfig.fieldName = 'value';
<#if parameters.width?exists>
    fieldConfig.width = ${parameters.width};
</#if>
    fieldConfig.value = data;
    var list = new ZUTUBI.StringList(fieldConfig);
    form.add(list);
<#if form.displayMode?default(false)>
    list.on('change', updateButtons);
</#if>
}());

<#include "/prototype/xhtml/controlfooter.ftl" />
