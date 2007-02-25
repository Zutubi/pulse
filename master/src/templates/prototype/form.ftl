<#-- render form -->
<#assign style="xhtml">
<#if form?exists>
    <form id="${form.id}" method="post" action="${form.action}">
        <ul>
        <#list form.fields as field>
        <li>
            <#assign parameters=field.parameters>
            <#if parameters.type == "select">
                <#include "${style}/select.ftl"/>
            <#elseif parameters.type == "hidden">
                <#include "${style}/hidden.ftl"/>
            <#elseif parameters.type == "password">
                <#include "${style}/password.ftl"/>
            <#elseif parameters.type == "textarea">
                <#include "${style}/textarea.ftl"/>
            <#else>
                <#include "${style}/text.ftl"/>
            </#if>
        </li>
        </#list>
        <li>
        <#list form.actions as action>
            &nbsp;<input type="submit" name="submit" value="${action}"/>
        </#list>
        </li>
        </ul>
    </form>

    <#-- use jscript to wire up the event handlers for supporting inline form validation -->
    <#-- use jscript to wire up the event handlers for handling the on 'enter' form submission -->

</#if>