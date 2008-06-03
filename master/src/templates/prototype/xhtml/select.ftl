<#include "/prototype/xhtml/controlheader.ftl" />

(function()
{
    var data = [];
    var value = [];

    <#list parameters.list as item>
        <#if parameters.listKey?exists>
            <#assign itemKey = item[parameters.listKey]/>
        <#else>
            <#assign itemKey = item/>
        </#if>
        <#if parameters.listValue?exists>
            <#assign itemValue = item[parameters.listValue]/>
        <#else>
            <#assign itemValue = item/>
        </#if>

    data.push(['${itemKey?js_string}', '${itemValue?js_string}']);

        <#if parameters.value?exists && (parameters.value?is_sequence && parameters.value?seq_contains(itemKey) || !parameters.value?is_sequence && parameters.value == itemKey)>
    value.push('${itemKey?js_string}');
        </#if>
    </#list>

    var store = new Ext.data.SimpleStore({
        fields: ['value', 'text'],
        data: data
    });

    fieldConfig.store = store;
    fieldConfig.value = value;
<#if parameters.multiple?exists>
    fieldConfig.multiple = true;
</#if>
<#if parameters.size?exists>
    fieldConfig.size = ${parameters.size};
</#if>
    var select = new ZUTUBI.Select(fieldConfig);
    form.add(select);
    select.on('change', updateButtons);

    form.add(new Ext.form.Hidden({name: '${parameters.name}.default', value: ''}));
}());

<#include "/prototype/xhtml/controlfooter.ftl" />
