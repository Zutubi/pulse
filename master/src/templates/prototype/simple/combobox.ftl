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
    fieldConfig.displayField = 'text';
    fieldConfig.editable = false;
    fieldConfig.forceSelection = true;
<#if parameters.value?exists>
    fieldConfig.value = '${parameters.value?js_string}';
<#else>
    if(data.length > 0)
    {
        fieldConfig.value = data[0][0];
    }
</#if>
    form.add(new Ext.form.ComboBox(fieldConfig));
}());
