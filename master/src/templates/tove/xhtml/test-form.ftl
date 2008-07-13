<#if form?exists>
<table class="eform"><tr><td id="${form.name}.status"></td></tr><tr><td id="${form.id}"><td></tr></table>
<script type="text/javascript">
    var ${form.name} = function()
    {
        var form = new ZUTUBI.CheckFormPanel(${mainFormName}, {
            method: 'POST',
            formName: '${form.name?js_string}',
            labelAlign: 'right',
            labelWidth: 150,
            border: false,
            waitMsgTarget: true,
            items: [{
                xtype: 'hidden',
                id: '${form.name?js_string}.submitField',
                name: 'submitField',
                value: ''
            }]
        });

        function submitForm()
        {
            var f = form.getForm();
            if(f.isValid())
            {
                f.clearInvalid();
                window.formSubmitting = true;
                f.submit({
                    clientValidation: false
                });
            }
        }

        <#list form.submitFields as submitField>
            form.addButton('${submitField.value}', function() { submitForm(); });
        </#list>

        var defaultSubmit = submitForm;

        <#include "/tove/xhtml/form-fields.ftl"/>

        return form;
    }();
</script>
</#if>
