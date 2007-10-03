<#--
A table is used as there is no good way to have a div collapse to the
width of its contents.  Floating it works, but hurts other things.
-->
<table class="eform"><tr><td id="${form.name}.status"></td></tr><tr><td id="${form.id}"><td></tr></table>

<#-- render form -->
<script type="text/javascript">
    Ext.QuickTips.init();
    Ext.QuickTips.tagConfig.width = 'qwidth';

    Ext.form.Field.prototype.msgTarget = 'under';

    var ${form.name} = function()
    {
        var form = new ZUTUBI.Form({
            method: 'post',
            waitMsgTarget: 'center'
        });

        function submitForm(value)
        {
            Ext.get('submitField').dom.value = value;
    <#if form.ajax>
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
