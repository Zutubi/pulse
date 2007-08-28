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
    var inheritedEl = Ext.DomHelper.append(field.getEl().dom.parentNode, { tag: 'img', alt: 'inherited', src: '${base}/images/inherited.gif', id: '${parameters.id}.inherited'}, true);
    inheritedEl.alignTo(inheritedEl.getPrevSibling(), 'l-r');
    inheritedEl.dom.qtip = 'value inherited from ${parameters.inheritedFrom}';
    field.getEl().addClass('field-inherited');
</#if>

<#if parameters.overriddenOwner?exists>
    var inheritedEl = Ext.DomHelper.append(field.getEl().dom.parentNode, { tag: 'img', alt: 'overridden', src: '${base}/images/overridden.gif', id: '${parameters.id}.overridden'}, true);
    inheritedEl.alignTo(inheritedEl.getPrevSibling(), 'l-r');
    inheritedEl.dom.qtip = 'overrides value defined by ${parameters.overriddenOwner}';
    field.getEl().addClass('field-overridden');
</#if>

<#assign helpkey>${parameters.name}.help</#assign>
<#assign helpmsg>${helpkey?i18n}</#assign>
<#if helpmsg?exists && helpkey != helpmsg>
    var xFormElement = field.getEl().findParent('.x-form-element')
    Ext.DomHelper.append(xFormElement, { tag: 'span', cls: 'inline-help', html: '${helpmsg?js_string}', id:field.getId() + '-inline-help'});
</#if>
});

<#if parameters.scripts?exists>
    <#list parameters.scripts as script>
        <#include "/${script}.ftl"/>
    </#list>
</#if>
