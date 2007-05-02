<#-- render form -->
<#if form?exists>
<form id="${form.id}" method="post" action="${base}/${form.action}">

    <table class="form">
    <#list form.fields as field>
        <#assign parameters=field.parameters>
        <#include "${parameters.type}.ftl"/>
    </#list>

        <#-- submit field required by javascript submit support -->
        <input type="hidden" name="submitField"></input>
        
    <#include "submitgroup.ftl"/>
    <#list form.submitFields as submitField>
        <#assign parameters=submitField.parameters>
        <#include "submit.ftl"/>
    </#list>
    <#include "submitgroup-end.ftl"/>

    </table>
</form>

    <#-- TODO: use jscript to wire up the event handlers for supporting inline form validation -->

    <script language="javascript">
        function submitenter(evt, value)
        {
            // provide backward compatibility. The value may not be specified, in which case default to 'next'.
            if (!value)
            {
                value = "next"; // the default value.
            }

            if (evt.getKey() == Ext.EventObject.RETURN)
            {
                var field = evt.getTarget();
                field.form.submitField.value = value;
                field.form.submit();
                return false;
            }
            else
            {
                return true;
            }
        }

        <#-- use jscript to wire up the event handlers for handling the on 'enter' form submission -->
        <#assign defaultsubmit='save'>
        <#list form.submitFields as submitField>
            <#if submitField.name == 'next'>
                <#assign defaultsubmit='next'>
            <#elseif submitField.name == 'finish' && defaultsubmit != 'next'>
                <#assign defaultsubmit='finish'>
            </#if>
        </#list>
        <#list form.fields as field>
            <#if field.type != 'hidden' && field.type != 'select'>
            Ext.get('${field.name}').on('keypress', function(event){ return submitenter(event, '${defaultsubmit}');});
            </#if>
        </#list>

        <#-- focus on the first form element. -->
        Ext.onReady(function(){ Ext.get('${form.fields[0].name}').focus() });
    </script>

</#if>