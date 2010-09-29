<#include "/tove/xhtml/controlheader.ftl" />

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

    fc.store = store;
    fc.value = value;
<#if parameters.multiple?exists>
    fc.multiple = true;
</#if>
<#if parameters.size?exists>
    fc.size = ${parameters.size};
</#if>

    ${form.name}.add(new Ext.form.Hidden({name: '${parameters.name}.default', value: ''}));

    var select = new ZUTUBI.form.Select(fc);
    ${form.name}.add(select);
    select.on('change', updateButtons);
}());

<#include "/tove/xhtml/controlfooter.ftl" />
