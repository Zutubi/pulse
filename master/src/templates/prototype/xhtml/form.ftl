<#-- render form -->
<script type="text/javascript">
    Ext.QuickTips.init();    
    Ext.form.Field.prototype.msgTarget = 'side';
    
    var ${form.name} = function()
    {
        var form = new Ext.form.Form({
            method: 'post',
            labelAlign: 'right',
            labelWidth: 150,
            waitMsgTarget: true
        });

        function submitForm(value)
        {
            Ext.get('submitField').dom.value = value;
    <#if form.ajax>
            form.submit({
                clientValidation: value != 'cancel',
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

        <#list form.submitFields as submitField>
            <#if submitField.parameters.default?exists>
                defaultSubmit = function()
                {
                    submitForm('${submitField.value}');
                }
            </#if>

            form.addButton('${submitField.value}', function() { submitForm('${submitField.value}'); });
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

        form.on('render', function()
        {
            var errorMessage;

    <#list form.fields as field>
        <#assign parameters=field.parameters>
        <#if fieldErrors?exists && fieldErrors[parameters.name]?exists>
            errorMessage = '<#list fieldErrors[parameters.name] as error>${error?i18n?js_string}<br/></#list>';
            form.findField('${parameters.id}').markInvalid(errorMessage);
        </#if>
        <#if field.type == 'text' || field.type == 'password' || field.type == 'checkbox'>
            Ext.get('${field.id}').on('keypress', function(event){ return handleKeypress(event);});
        </#if>
    </#list>

            Ext.get('${form.fields[0].id}').focus()
        });

        <#include "/prototype/xhtml/form-fields.ftl"/>

        return form;
    }();
</script>

<div id="${form.id}" style="width: 350px"></div>
