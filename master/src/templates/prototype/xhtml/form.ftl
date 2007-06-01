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

        form.on('render', function() { form.items.first().focus(); });

        <#include "/prototype/xhtml/form-fields.ftl"/>

        return form;
    }();
</script>
<#--
A table is used as there is no good way to have a div collapse to the
width of its contents.  Floating it works, but hurts other things.
-->
<table class="eform"><tr><td id="${form.name}.status"></td></tr><tr><td id="${form.id}"><td></tr></table>