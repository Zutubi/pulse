<#-- render form -->
<#if form?exists>
<form id="${form.id}" method="post" action="${base}/${form.action}">

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

    <#-- include prototype.js on every page?... need the ${base} -->
    <script type="text/javascript" src="/js/prototype.js"></script>
    <script language="javascript">

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
            Event.observe($('${field.name}'), 'keypress', function(event){ return submitenter($('${field.name}'), event, '${defaultsubmit}');});
            </#if>
        </#list>

        <#-- focus on the first form element. -->
        <#-- does not seem to work..
            Field.focus($('${form.fields[0].name}'));        
        -->

        function submitenter(field, evt, value)
        {
            // provide backward compatibility. The value may not be specified, in which case default to 'next'.
            if (!value)
            {
                value = "next"; // the default value.
            }

            var keycode;
            if (window.event)
            {
                keycode = window.event.keyCode;
            }
            else if (evt)
            {
                keycode = evt.which;
            }
            else
            {
                return true;
            }

            if (keycode == 13)
            {
                field.form.submitField.value = value;
                field.form.submit();
                return false;
            }
            else
            {
                return true;
            }
        }

    </script>

</#if>