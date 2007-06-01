<#if form?exists>
<script type="text/javascript" src="${base}/js/zutubi.js"></script>
<script type="text/javascript">
    var ${form.name} = function()
    {
        var form = new ZUTUBI.CheckForm(${mainFormName}, {
            method: 'post',
            labelAlign: 'right',
            labelWidth: 150,
            waitMsgTarget: true
        });

        function submitForm()
        {
            if(form.isValid())
            {
                form.submit({
                    clientValidation: true,
                    waitMsg: 'Testing...'
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

<div id="${form.id}" style="width: 350px"></div>
</#if>