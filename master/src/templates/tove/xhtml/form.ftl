<#--
A table is used as there is no good way to have a div collapse to the
width of its contents.  Floating it works, but hurts other things.
-->
<table class="eform">
    <tr>
        <td id="${form.name}.status">
<#if actionErrors?exists>
            <ul class="validation-error">
    <#list actionErrors as error>
                <li>${error?html}</li>
    </#list>
            </ul>
</#if>
        </td>
    </tr>
    <tr>
        <td class="form-cell" id="${form.id}"></td>
    </tr>
</table>

<#-- render form -->
<script type="text/javascript">
    Ext.QuickTips.init();
    Ext.QuickTips.getQuickTip().qwidth = 'qwidth';

    Ext.form.Field.prototype.msgTarget = 'under';

    var ${form.name} = function()
    {
        var form = new ZUTUBI.FormPanel({
            method: 'POST'
            , formName: '${form.name?js_string}'
            , waitMsgTarget: 'nested-layout'
            , border: false
            , labelWidth: 'hello'
            , items: [{
                xtype: 'hidden',
                id: '${form.name?js_string}.submitField',
                name: 'submitField',
                value: 'h'
            }]
<#if form.fileUpload>
            , fileUpload: true
</#if>
        });

        function submitForm(value)
        {
            var f = form.getForm();

            Ext.get('${form.name?js_string}.submitField').dom.value = value;
            if(value == 'cancel')
            {
                Ext.DomHelper.append(f.el.parent(), {tag: 'input', type: 'hidden', name: 'cancel', value: 'true'});
            }

            f.clearInvalid();
    <#if form.ajax>
            window.formSubmitting = true;
            f.submit({
                clientValidation: false,
                waitMsg: 'Submitting...'
            });
    <#else>
            if(value == 'cancel' || f.isValid())
            {
                f.el.dom.submit();
            }
    </#if>
        }

        var defaultSubmit = function() {};
        var buttonConfig;

    <#if !form.readOnly>
        <#list form.submitFields as submitField>
            <#if submitField.parameters.default?exists>
                defaultSubmit = function()
                {
                    submitForm('${submitField.value}');
                }
            </#if>

            buttonConfig = { text: '${submitField.value}' };
            <#if form.displayMode?default(false)>
                buttonConfig.disabled = true;
            </#if>
            form.addButton(buttonConfig, function() { submitForm('${submitField.value}'); });
        </#list>
    </#if>
    
        <#include "/tove/xhtml/form-fields.ftl"/>

        return form;
    }();
</script>
