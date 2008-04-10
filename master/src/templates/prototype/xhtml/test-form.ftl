<#if form?exists>
<table class="eform"><tr><td id="${form.name}.status"></td></tr><tr><td id="${form.id}"><td></tr></table>
<script type="text/javascript">
    var ${form.name} = function()
    {
        var form = new ZUTUBI.CheckForm(${mainFormName}, {
            method: 'post',
            formName: '${form.name?js_string}',
            labelAlign: 'right',
            labelWidth: 150,
            waitMsgTarget: true
        });

        function submitForm()
        {
            if(form.isValid())
            {
                form.clearInvalid();
                window.formSubmitting = true;
                form.submit({
                    clientValidation: false
                });
            }
        }

        <#list form.submitFields as submitField>
            form.addButton('${submitField.value}', function() { submitForm(); });
        </#list>

        var defaultSubmit = submitForm;

        <#include "/prototype/xhtml/form-fields.ftl"/>

        return form;
    }();
</script>
</#if>
