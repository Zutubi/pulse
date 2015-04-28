${form.name}.on('actioncomplete', function() { window.formSubmitting = false; });
${form.name}.on('actionfailed', function() { window.formSubmitting = false; });
<#if form.displayMode?default(false)>
    ${form.name}.displayMode = true;
</#if>

function updateButtons()
{
    ${form.name}.updateButtons();
}
</script>

<#list form.fields as field>
<#-- Each field gets its own script element to avoid hitting a WebKit limit (CIB-1675) -->
<script type="text/javascript">
    var fc;
    <#assign parameters=field.parameters>
    fc = {
    <#if form.readOnly>
        disabled: true,
    <#elseif parameters.readOnly?default(false)>
        readOnly: true,
        cls:'x-item-disabled',
    </#if>
        width: 360,
        validateOnBlur: false
    };
    <#include "${field.type}.ftl"/>
</script>
</#list>

<script type="text/javascript">
Ext.onReady(function()
{
    Ext.Ajax.timeout = 60000;

    ${form.name}.render('${form.id?js_string}');

    var f = ${form.name}.getForm();
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

<#list form.fields as field>
    <#assign parameters=field.parameters>
    <#if fieldErrors?exists && fieldErrors[field.name]?exists>
    ${form.name}.findById('${field.id?js_string}').markInvalid('<#list fieldErrors[field.name] as error>${error?i18n?js_string}<br/></#list>');
    </#if>
</#list>

    ${form.name}.attachFieldKeyHandlers();

<#if !form.readOnly>
    var buttonEl;
    <#list form.submitFields as submitField>
        buttonEl = ${form.name}.buttons[${submitField_index}].el.child('button:first');
        buttonEl.set({tabindex: window.nextTabindex++ });
        buttonEl.dom.id = buttonEl.id = 'zfid.${submitField.value?js_string}';
    </#list>
</#if>
    ${form.name}.rendered = true;
    ${form.name}.fireEvent('render', ${form.name});
});
