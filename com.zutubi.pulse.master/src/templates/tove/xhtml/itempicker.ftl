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
<#if parameters.allowReordering?exists>
    fc.allowReordering = ${parameters.allowReordering?string};
</#if>
<#if parameters.width?exists>
    fc.width = ${parameters.width};
</#if>

    ${form.name}.add(new Ext.form.Hidden({name: '${parameters.name}.default', value: ''}));

    var picker = new ZUTUBI.ItemPicker(fc);
    ${form.name}.add(picker);
    picker.on('change', updateButtons);

<#if parameters.dependentOn?exists>

    var dependentField = ${form.name}.findField('zfid.${parameters.dependentOn}');
    if (dependentField) {
        dependentField.on('select', function(){
            var pane = Ext.get('nested-layout');
            pane.mask('Please wait...');
            window.actionInProgress = true;
            picker.clear();
            Ext.Ajax.request({
                url: window.baseUrl + '/aconfig/${path}?options',
                method: 'POST',
                params: {field:'${parameters.name}', dependency:dependentField.getValue()},
                success: function(result, request){
                    optionString = Ext.util.JSON.decode(result.responseText);
                    picker.loadOptions(optionString, false);
                    pane.unmask();
                    window.actionInProgress = false;
                },
                failure: function(result, request){
                    showStatus('failed to update dependency stages options', 'failure');
                    pane.unmask();
                    window.actionInProgress = false;
                }
            });
        });
    }
</#if>

}());

<#include "/tove/xhtml/controlfooter.ftl" />
