<#include "/prototype/xhtml/controlheader.ftl" />

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

    fieldConfig.optionStore = optionStore;
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

    fieldConfig.store = store;
    fieldConfig.value = value;
<#if parameters.width?exists>
    fieldConfig.width = ${parameters.width};
</#if>

    var picker = new ZUTUBI.ItemPicker(fieldConfig);
    form.add(picker);
    picker.on('change', updateButtons);

    hiddenFields.push({name: '${parameters.name}.default', value: ''});
}());

<#include "/prototype/xhtml/controlfooter.ftl" />
