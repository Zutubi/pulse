form.items.last().on('render', function(field)
{
<#if parameters.actions?exists>
    <#list parameters.actions as action>
        <#assign i18nKey = "${parameters.name}.${action}"/>
        var linkEl = Ext.DomHelper.append(field.getEl().dom.parentNode, { tag: 'a', href: '#', html: '${i18nKey?i18n?js_string}', cls: 'field-action', id: '${parameters.id}.${action?js_string}'}, true);
        linkEl.on('click', function(e) { field.fireEvent('${action?js_string}', field); e.preventDefault(); });
    </#list>
</#if>

<#if parameters.noOverride?exists>
    field.getEl().addClass('field-no-override');
</#if>

<#if parameters.inheritedFrom?exists>
    form.annotateField('${parameters.id}', '${base}/images/inherited.gif', 'value inherited from ${parameters.inheritedFrom}');
</#if>

<#if parameters.overriddenOwner?exists>
    form.annotateField('${parameters.id}', '${base}/images/overridden.gif', 'overrides value defined by ${parameters.overriddenOwner}');
</#if>

<#assign helpkey>${parameters.name}.help</#assign>
<#assign helpmsg>${helpkey?i18n}</#assign>
<#if helpmsg?exists && helpkey != helpmsg>
    form.annotateField('${parameters.id}', '${base}/images/help.gif', '${helpmsg?js_string}');
</#if>
});

<#if parameters.scripts?exists>
    <#list parameters.scripts as script>
        <#include "/${script}.ftl"/>
    </#list>
</#if>
