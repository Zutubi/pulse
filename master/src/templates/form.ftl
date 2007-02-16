<#-- render form -->
<#if form?exists>
    <form id="${form.id}" method="post" action="${form.action}">
        <ul>
        <#list form.fields as field>
        <li>
            <#if field.parameters.label?exists>
            <label>${field.parameters.label?i18n?html}</label>
            </#if>
            <#if field.parameters.type == "select">
                <#assign parameters=field.parameters>
                <#include "select.ftl"/>
            <#else>
                <input <#if field.parameters.type?exists> type="${field.parameters.type}" </#if> name="${field.parameters.name}" tabindex="${field.tabindex}" <#if field.parameters.value?exists> value="${field.parameters.value}" </#if>/>
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

</#if>