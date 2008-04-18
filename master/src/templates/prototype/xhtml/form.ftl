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
        <td id="${form.id}"><td>
    </tr>
</table>

<#-- render form -->
<script type="text/javascript">
    Ext.QuickTips.init();
    Ext.QuickTips.tagConfig.width = 'qwidth';

    Ext.form.Field.prototype.msgTarget = 'under';

    var ${form.name} = function()
    {
        var form = new ZUTUBI.Form({
            method: 'post'
            , formName: '${form.name?js_string}'
            , waitMsgTarget: 'center'
<#if form.fileUpload>
            , fileUpload: true
            , autoCreate: {tag: 'form', method: 'post', id: Ext.id(), enctype: 'multipart/form-data' }
</#if>
        });

        function submitForm(value)
        {
            Ext.get('${form.name?js_string}.submitField').dom.value = value;
            if(value == 'cancel')
            {
                Ext.DomHelper.append(form.el, {tag: 'input', type: 'hidden', name: 'cancel', value: 'true'});
            }

            form.clearInvalid();
    <#if form.ajax>
            window.formSubmitting = true;
            form.submit({
                clientValidation: false,
                waitMsg: 'Submitting...'
            });
    <#else>
            if(value == 'cancel' || form.isValid())
            {
                form.el.dom.submit();
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
    
        <#include "/prototype/xhtml/form-fields.ftl"/>

        return form;
    }();
</script>
