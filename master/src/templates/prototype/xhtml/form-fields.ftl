var fieldConfig;
var hiddenFields = [];

hiddenFields.push({id: 'submitField', name: 'submitField', value: ''});

<#list form.fields as field>
    <#assign parameters=field.parameters>
    fieldConfig = { width: 200 };
    <#include "${parameters.type}.ftl"/>
</#list>

function handleKeypress(evt)
{
    if (evt.getKey() == evt.RETURN)
    {
        defaultSubmit();
        evt.preventDefault();
        return false;
    }
    else
    {
        return true;
    }
}

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

    form.el.dom.name = '${form.name}';

    var errorMessage;
    if (!window.nextTabindex)
    {
       <#-- Tabs start at 100 to avoid issues with tabindexs set on other HTML
            elements (e.g. tree nodes have index 1). -->
        window.nextTabindex = 100;
    }

<#list form.fields as field>
    <#assign parameters=field.parameters>
    <#if fieldErrors?exists && fieldErrors[parameters.name]?exists>
    errorMessage = '<#list fieldErrors[parameters.name] as error>${error?i18n?js_string}<br/></#list>';
    form.findField('${parameters.id}').markInvalid(errorMessage);
    </#if>

    var el = Ext.get('${field.id}');
    if(el)
    {
        el.set({tabindex: window.nextTabindex++ });
    <#if parameters.submitOnEnter>
        el.on('keypress', function(event){ return handleKeypress(event); });
    </#if>
    }
</#list>

<#list form.submitFields as submitField>
    form.buttons[${submitField_index}].el.select('button').set({tabindex: window.nextTabindex++ });
</#list>

    form.rendered = true;
    form.fireEvent('render', form);
});
