<#include "/tove/xhtml/controlheader.ftl" />

(function()
{
<#if parameters.list?exists>
    var optionData = [];

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

    optionData.push(['${itemKey?js_string}', '${itemValue?js_string}']);
    </#list>

    var optionStore = new Ext.data.SimpleStore({
        fields: ['value', 'text'],
        data: optionData
    });

    fc.optionStore = optionStore;
</#if>

    var value = [];

<#if parameters.value?exists>
    <#list parameters.value as item>
        value.push('${item?js_string}');
    </#list>
</#if>

    var store = new Ext.data.SimpleStore({
        fields: ['value', 'text'],
        data: []
    });

    fc.store = store;
    fc.value = value;
<#if parameters.width?exists>
    fc.width = ${parameters.width};
</#if>

    form.add(new Ext.form.Hidden({name: '${parameters.name}.default', value: ''}));

    var picker = new ZUTUBI.ItemPicker(fc);
    form.add(picker);
    picker.on('change', updateButtons);
}());

<#include "/tove/xhtml/controlfooter.ftl" />
