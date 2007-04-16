<#-- render form -->
<#if form?exists>
<#-- This form requires javascript to behave correctly. Only display it if javascript is enabled. -->
<div id="${form.id}_div" style="display:none">
    <form id="${form.id}" method="post" action="${base}/${form.action}">

        <#-- render the test form. -->
        <table class="form">
        <#list form.fields as field>
            <#assign parameters=field.parameters>
            <#if parameters.type == "select">
                <#include "select.ftl"/>
            <#elseif parameters.type == "hidden">
                <#include "hidden.ftl"/>
            <#elseif parameters.type == "password">
                <#include "password.ftl"/>
            <#elseif parameters.type == "textarea">
                <#include "textarea.ftl"/>
            <#elseif parameters.type == "checkbox">
                <#include "checkbox.ftl"/>
            <#else>
                <#include "text.ftl"/>
            </#if>
        </#list>

        <#-- submit field required by javascript submit support -->
        <input type="hidden" name="submitField"/>

        <#-- render the forms submit buttons -->
        <#include "submitgroup.ftl"/>
        <#list form.submitFields as submitField>
            <#assign parameters=submitField.parameters>
            <#include "submit.ftl"/>
        </#list>
        <#include "submitgroup-end.ftl"/>

        </table>
    </form>

    <#-- TODO: use jscript to wire up the event handlers for supporting inline form validation -->

    <script type="text/javascript" src="/js/prototype.js"></script>
    <script language="javascript">
    
        <#-- only show the test form if javascript is enabled, since it will not work without it. -->
        $('${form.id}_div').style.display = '';

        <#-- loops through the form fields, transfering the form values into the hidden fields. -->
        Event.observe($('${form.id}_div'), 'submit', function(event){ return submitcheck(event); });

        function submitcheck(evt)
        {
        <#list form.parameters.originalFields as fieldName>
            $('${fieldName}_check').value = $F('${fieldName}');
        </#list>
            $('${form.id}').submit();
            return false;
        }
    </script>
</div>
</#if>