<#if form?exists>
<table class="eform"><tr><td id="${form.name}-status"></td></tr><tr><td id="${form.id}"><td></tr></table>
<script type="text/javascript">
    var ${form.name} = new Zutubi.form.CheckFormPanel(${mainFormName}, {
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

    <#list form.submitFields as submitField>
        ${form.name}.addButton('${submitField.value}', function() { ${form.name}.defaultSubmit(); });
    </#list>

    <#include "/tove/xhtml/form-fields.ftl"/>
</script>
</#if>