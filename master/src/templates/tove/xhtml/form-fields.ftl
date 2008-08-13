form.on('actioncomplete', function() { window.formSubmitting = false; });
form.on('actionfailed', function() { window.formSubmitting = false; });
<#if form.displayMode?default(false)>
    form.displayMode = true;
</#if>

var fieldConfig;

<#list form.fields as field>
    <#assign parameters=field.parameters>
    fieldConfig = {
        width: 360
      , validateOnBlur: false
    <#if form.readOnly>
      , disabled: true
    </#if>
    };
    <#include "${parameters.type}.ftl"/>
</#list>

function handleKeypress(evt)
{
<#if form.readOnly>
    return true;
<#else>
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
</#if>
}

function updateButtons()
{
    form.updateButtons();
}

Ext.onReady(function()
{
    Ext.Ajax.timeout = 60000;

    form.render('${form.id?js_string}');

    var f = form.getForm();
    f.el.dom.action = '${base}/${form.action?js_string}';
    f.url = '${base}/${form.action?js_string}';
    f.el.set({name: '${form.name?js_string}'});

    var errorMessage;
    if (!window.nextTabindex)
    {
       <#-- Tabs start at 100 to avoid issues with tabindexs set on other HTML
            elements (e.g. tree nodes have index 1). -->
        window.nextTabindex = 100;
    }

    var field;
<#list form.fields as field>
    <#assign parameters=field.parameters>
    field = form.findById('${parameters.id?js_string}');
    <#if fieldErrors?exists && fieldErrors[parameters.name]?exists>
    errorMessage = '<#list fieldErrors[parameters.name] as error>${error?i18n?js_string}<br/></#list>';
    field.markInvalid(errorMessage);
    </#if>

    var el = Ext.get('${field.id}');
    if(el)
    {
        if (field.getXType() == 'checkbox')
        {
            el = field.innerWrap;
        }

        el.set({tabindex: window.nextTabindex++ });

    <#if parameters.submitOnEnter?default(true)>
        el.on('keypress', function(event){ return handleKeypress(event); });
    </#if>
        el.on('keyup', updateButtons);
        el.on('click', updateButtons);
    }
</#list>

<#if !form.readOnly>
    var buttonEl;
    <#list form.submitFields as submitField>
        buttonEl = form.buttons[${submitField_index}].el.child('button:first');
        buttonEl.set({tabindex: window.nextTabindex++ });
        buttonEl.dom.id = buttonEl.id = 'zfid.${submitField.value?js_string}';
    </#list>
</#if>
    form.rendered = true;
    form.fireEvent('render', form);
});
