<#include "/tove/xhtml/controlheader.ftl" />

(function()
{
    var data = [];
<#list parameters.list as item>
    data.push('${item?js_string}');
</#list>

    fc.store = data;
    fc.mode = 'local';
    fc.triggerAction = 'all';
    fc.editable = true;
    fc.forceSelection = false;
<#if parameters.width?exists>
    fc.width = ${parameters.width};
</#if>
<#if parameters.value?exists>
    fc.value = '${parameters.value?js_string}';
<#else>
    if(data.length > 0)
    {
        fc.value = data[0];
    }
</#if>
    var combo = new Ext.form.ComboBox(fc);
    ${form.name}.add(combo);
    combo.on('select', updateButtons);
    combo.on('keyup', function() { combo.setValue(combo.getRawValue()); });
}());

<#include "/tove/xhtml/controlfooter.ftl" />
