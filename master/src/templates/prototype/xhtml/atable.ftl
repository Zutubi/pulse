<#macro annotation baseName id qtip>
    <#assign annotationCount = annotationCount + 1/>
    <#assign annotationId = "${id}:${baseName}"/>
    <img id="${annotationId?id}" src="${base}/images/${id}.gif" alt="${id}"/>
    <script type="text/javascript>
        Ext.get("${annotationId?id?js_string}").dom.qtip = "${qtip}";
    </script>
</#macro>

<table id="config.table" width="60%">
    <tr>
        <th class="heading" colspan="${table.width?c}">${table.heading?html}</th>
    </tr>
    <tr>
<#list table.headers as header>
        <th class="content">${header?html}</th>
</#list>
        <th class="content" colspan="2">${"actions.label"?i18n}</th>
    </tr>
<#list table.rows as row>
    <#assign rawId = "item:${row.baseName}"/>
    <tr id="${rawId?id}"
    ><#t/>
    <#-- Data cells. -->
    <#list row.cells as cell>
        <td class="content ${row.parameters.cls?default('')}" colspan="${cell.span?c}">${cell.content?html}</td>
    </#list>

    <#if row.actions?exists>
        <#-- Annotations, e.g. template decoration. -->
        <td class="content" width="1%">
        <#assign annotationCount = 0/>
        <#if row.parameters.inheritedFrom?exists>
            <@annotation id="inherited" baseName=row.baseName qtip="inherited from ${row.parameters.inheritedFrom}"/>
        </#if>
        <#if row.parameters.overriddenOwner?exists>
            <@annotation id="overridden" baseName=row.baseName qtip="overrides value defined by ${row.parameters.overriddenOwner}"/>
        </#if>
        <#if row.parameters.hiddenFrom?exists>
            <@annotation id="hidden" baseName=row.baseName qtip="hidden item defined at ${row.parameters.hiddenFrom}"/>
        </#if>
        <#if annotationCount == 0>
            &nbsp;
        </#if>
        </td>

        <#-- Action links. -->
        <td class="content" width="1%">
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
                onclick="actionPath('${row.path}?${rowAction.action}'); return false;">
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