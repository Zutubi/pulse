<#include "/tove/xhtml/controlheader.ftl" />

(function()
{
<#if parameters.list?exists>
    <#-- od is short for optionData, shrunk to reduce javascript bloat -->
    var od = [];

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

    od.push(['${itemKey?js_string}', '${itemValue?js_string}']);
    </#list>

    var optionStore = new Ext.data.SimpleStore({
        fields: ['value', 'text'],
        data: od
    });

    fc.optionStore = optionStore;
</#if>

    var v = [];

<#if parameters.value?exists>
    <#list parameters.value as item>
        v.push('${item?js_string}');
    </#list>
</#if>

    var store = new Ext.data.SimpleStore({
        fields: ['value', 'text'],
        data: [],
        sortInfo: {'field': 'text', 'direction': 'ASC'}
    });

    fc.store = store;
    fc.value = v;
<#if parameters.ordered?exists>
    fc.ordered = ${parameters.ordered?string};
</#if>
<#if parameters.width?exists>
    fc.width = ${parameters.width};
</#if>

    ${form.name}.add(new Ext.form.Hidden({name: '${parameters.name}.default', value: ''}));

    var picker = new ZUTUBI.ItemPicker(fc);
    ${form.name}.add(picker);
    picker.on('change', updateButtons);
}());

<#include "/tove/xhtml/controlfooter.ftl" />
