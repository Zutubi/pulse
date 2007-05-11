var fieldConfig;
var hiddenFields = [];

hiddenFields.push({id: 'submitField', name: 'submitField', value: ''});

<#list form.fields as field>
    <#assign parameters=field.parameters>
    fieldConfig = { width: 200 };
    <#include "${parameters.type}.ftl"/>
</#list>

<#-- focus on the first form element. -->
Ext.onReady(function()
{
    form.render('${form.id}');
    form.el.dom.action = '${base}/${form.action}';
    for(var i = 0; i < hiddenFields.length; i++)
    {
        var config = hiddenFields[i];
        config.tag = 'input';
        config.type = 'hidden';
        Ext.DomHelper.append(form.el, config);
    }

    Ext.get(form.el).on('keypress', function(e) { if (e.getKey() == e.RETURN) { e.preventDefault(); } });
    form.rendered = true;
    form.fireEvent('render', form);
});
