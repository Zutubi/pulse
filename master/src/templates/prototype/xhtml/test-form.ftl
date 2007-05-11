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

        function handleKeypress(evt)
        {
            if (evt.getKey() == evt.RETURN)
            {
                submitForm();
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
    <#list form.fields as field>
        <#assign parameters=field.parameters>
        <#if field.type == 'text' || field.type == 'password' || field.type == 'checkbox'>
            Ext.get('${field.id}').on('keypress', function(event){ return handleKeypress(event); });
        </#if>
    </#list>
        });

        <#include "/prototype/xhtml/form-fields.ftl"/>

        return form;
    }();
</script>

<div id="${form.id}" style="width: 350px"></div>
</#if>