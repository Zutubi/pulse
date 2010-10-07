<#include "/tove/xhtml/controlheader.ftl" />

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

        <#if itemValue == "">
            <#assign itemValue = "[not set]"/>
        </#if>
    data.push(['${itemKey?js_string}', '${itemValue?js_string}']);
    </#list>

    var store = new Ext.data.SimpleStore({
        fields: ['value', 'text'],
        data: data
    });

    fc.store = store;
    fc.mode = 'local';
    fc.hiddenName = fc.name;
    fc.name = 'combo.' + fc.hiddenName;
    fc.displayField = 'text';
    fc.valueField = 'value';
    fc.tpl = '<tpl for="."><div class="x-combo-list-item">{text:htmlEncode}</div></tpl>';
    fc.editable = false;
    fc.forceSelection = true;
    fc.triggerAction = 'all';
<#if parameters.width?exists>
    fc.width = ${parameters.width};
</#if>
<#if parameters.value?exists>
    fc.value = '${parameters.value?js_string}';
<#else>
    if(data.length > 0)
    {
        fc.value = data[0][0];
    }
</#if>
    var combo = new Ext.form.ComboBox(fc);
    ${form.name}.add(combo);
    combo.on('select', updateButtons);
}());

<#include "/tove/xhtml/controlfooter.ftl" />
