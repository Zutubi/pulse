<#include "/prototype/xhtml/controlheader.ftl" />

(function()
{
    var data = [];

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
    </#list>

    var store = new Ext.data.SimpleStore({
        fields: ['value', 'text'],
        data: data
    });

    fieldConfig.store = store;
    fieldConfig.mode = 'local';
    fieldConfig.hiddenName = fieldConfig.name;
    fieldConfig.name = 'combo.' + fieldConfig.hiddenName;
    fieldConfig.displayField = 'text';
    fieldConfig.valueField = 'value';
    fieldConfig.editable = false;
    fieldConfig.forceSelection = true;
    fieldConfig.triggerAction = 'all';
    fieldConfig.emptyText = 'jeebus';
<#if parameters.width?exists>
    fieldConfig.width = ${parameters.width};
</#if>
<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?js_string}';
<#else>
    if(data.length > 0)
    {
        fieldConfig.value = data[0][0];
    }
</#if>
    var combo = new Ext.form.ComboBox(fieldConfig);
    form.add(combo);
<#if form.displayMode?default(false)>
    combo.on('select', updateButtons);
</#if>
}());

<#include "/prototype/xhtml/controlfooter.ftl" />
