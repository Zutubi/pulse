<table id="config.table" width="60%">
    <tr>
        <th class="heading" colspan="${table.width?c}">${table.heading?html}</th>
    </tr>
    <tr>
<#list table.headers as header>
        <th class="content">${header?html}</th>
</#list>
        <th class="content">${"actions.label"?i18n}</th>
    </tr>
<#list table.rows as row>
    <#assign rawId = "item:${row.baseName}"/>
    <tr id="${rawId?id}">
    <#list row.cells as cell>
        <td class="content" colspan="${cell.span?c}">${cell.content?html}</td>
    </#list>
    <#if row.actions?exists>
        <td class="content" width="5%">
        <#if row.actions?size &gt; 0>
            <#assign first = true/>
            <#list row.actions as rowAction>
                <#assign actionId = "${rowAction.action}:${row.baseName}"/>
                <#if !first>
            |
                </#if>
            <a id="${actionId?id}"
                <#if rowAction.action == "view">
                    <#if embedded>
                        <#assign clickAction = "edit"/>
                    <#else>
                        <#assign clickAction = "select"/>
                    </#if>
                onclick="${clickAction}Path('${row.path}'); return false">
                <#elseif rowAction.action == "delete">
                onclick="deletePath('${row.path}'); return false;">
                <#else>
                onclick="actionPath('${row.path}?${action}'); return false;">
                </#if>
                ${rowAction.label?html}<#t>
            </a><#lt>
                <#assign first = false/>
            </#list>
        <#else>
        &nbsp;
        </#if>
        </td>
    </#if>
    </tr>
</#list>
<#if table.addAllowed>
    <tr>
        <td class="content" colspan="${table.width}"><a id="map:add" onclick="addToPath('${path}'); return false;">${"add.label"?i18n}</a></td>
    </tr>
</#if>
</table>