<#-- render form -->
<#if form?exists>
    <form id="${form.id}" method="post" action="config.action?path=${path}">
        <ul>
        <#list form.fields as field>
        <li>
            <label>${field.parameters.label?i18n?html}</label>
            <input <#if field.parameters.type?exists> type="${field.parameters.type}" </#if> name="${field.parameters.name}" tabindex="${field.tabindex}" <#if field.parameters.value?exists> value="${field.parameters.value}" </#if>/>
        </li>
        </#list>
        <li>
            <input type="submit" name="submit" value="save"/> <input type="submit" name="submit" value="cancel"/>
        </li>
        </ul>
    </form>

    <#-- use jscript to wire up the event handlers for supporting inline form validation -->

</#if>