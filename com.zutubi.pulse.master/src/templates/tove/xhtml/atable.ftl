<#macro annotation baseName id qtip>
    <#assign annotationCount = annotationCount + 1/>
    <#assign annotationId = "${id}:${baseName}"/>
    <img id="${annotationId?id}" src="${base}/images/${id}.gif" alt="${id}"/>
    <script type="text/javascript>
        Ext.get("${annotationId?id?js_string}").dom.qtip = "${qtip?js_string}";
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
<#if table.orderable>
        <th class="content">
           ${"order.label"?i18n}
<#if table.parameters.orderInheritedFrom?exists>
            <img id="order-inherited" src="${base}/images/inherited.gif" alt="order inherited"/>
            <script type="text/javascript>
                Ext.get("order-inherited").dom.qtip = "order inherited from ${table.parameters.orderInheritedFrom?js_string}";
            </script>
</#if>
<#if table.parameters.orderOverriddenOwner?exists>
            <img id="order-overridden" src="${base}/images/overridden.gif" alt="order overridden"/>
            <script type="text/javascript>
                Ext.get("order-overridden").dom.qtip = "overrides order defined by ${table.parameters.orderOverriddenOwner?js_string}";
            </script>
</#if>
       </th>
</#if>
        <th class="content" colspan="2">${"actions.label"?i18n}</th>
    </tr>
<#if table.rows?size == 0>
    <tr id="no.data">
        <td class="content" colspan="${table.width}">${"no.data.available"?i18n}</td>
    </tr>
</#if>
<#list table.rows as row>
    <#assign rawId = "item:${row.baseName}"/>
    <tr id="${rawId?id}"
    ><#t/>
    <#-- Data cells. -->
    <#list row.cells as cell>
        <td class="content ${row.parameters.cls?default('')}" colspan="${cell.span?c}">${cell.content?html}</td>
    </#list>

    <#-- Up/down ordering arrows -->
    <#if table.orderable>
        <td class="content" width="1%">
        <#if table.visibleRowCount == 1 || row.parameters.hiddenFrom?exists>
            &nbsp;
        <#else>
            <#if !table.isLastVisible(row)>
                <#assign actionId = "down:${row.baseName}"/>
            <a class="unadorned" id="${actionId?id}" href="#" onclick="actionPath('${row.path?js_string?html}', 'down', true); return false;"><img src="${base}/images/resultset_down.gif" alt="move down"/></a>
            </#if>
            <#if !table.isFirstVisible(row)>
                <#assign actionId = "up:${row.baseName}"/>
            <a class="unadorned" id="${actionId?id}" href="#" onclick="actionPath('${row.path?js_string?html}', 'up', true); return false;"><img src="${base}/images/resultset_up.gif" alt="move up"/></a>
            </#if>
        </#if>
        </td>
    </#if>

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
        <#assign rawId = "noan:${row.baseName}"/>
            <span id="${rawId?id}">-</span>
    </#if>
        </td>

    <#-- Action links. -->
        <td class="content" width="1%">
    <#if row.actions?size &gt; 0>
        <#assign first = true/>
        <#list row.actions as actionLink>
            <#assign actionId = "${actionLink.action}:${row.baseName}"/>
            <#if !first>
            |
            </#if>
            <a href="#" id="${actionId?id}" class="unadorned"
            <#if actionLink.action == "view">
                <#if embedded>
                    <#assign clickAction = "edit"/>
                <#else>
                    <#assign clickAction = "select"/>
                </#if>
                onclick="${clickAction}Path('${row.path?js_string?html}'); return false">
            <#elseif actionLink.action == "delete">
                onclick="deletePath('${row.path?js_string?html}'); return false;">
            <#else>
                onclick="actionPath('${row.path?js_string?html}', '${actionLink.action}', true); return false;">
            </#if>
            <#if actionLink.icon?exists>
                <img alt="${actionLink.label?html}" src="${base}/images/config/actions/${actionLink.icon}.gif"/>
            </#if>
                ${actionLink.label?html}<#t>
        </a><#lt>
            <#assign first = false/>
        </#list>
    <#else>
            &nbsp;
    </#if>
        </td>
    </tr>
</#list>
<#if table.addAllowed>
    <tr>
        <td class="content" colspan="${table.width}">
            <a id="map:add" class="unadorned" href="#" onclick="addToPath('${path?js_string?html}'); return false;">
                <img alt="add" src="${base}/images/config/actions/add.gif"/>
                ${"add.label"?i18n}
            </a>
        </td>
    </tr>
</#if>
</table>