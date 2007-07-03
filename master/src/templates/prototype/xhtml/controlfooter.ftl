form.items.last().on('render', function(field)
{
<#if parameters.browseLink?exists>
    var linkEl = Ext.DomHelper.append(field.getEl().dom.parentNode, { tag: 'a', href: '#', html: '${parameters.browseLink?i18n}', class: 'browse', id: '${parameters.id}.${parameters.browseLink}'}, true);
    linkEl.on('click', function(e) { field.fireEvent('browse', field); e.preventDefault(); });
</#if>

<#if parameters.noOverride?exists>
    field.getEl().addClass('field-no-override');
</#if>

<#if parameters.inheritedFrom?exists>
    var inheritedEl = Ext.DomHelper.append(field.getEl().dom.parentNode, { tag: 'img', alt: 'inherited', src: '${base}/images/inherited.gif', id: '${parameters.id}.inherited'}, true);
    inheritedEl.dom.qtip = 'value inherited from ${parameters.inheritedFrom}';
    field.getEl().addClass('field-inherited');
</#if>

<#if parameters.overriddenOwner?exists>
    var inheritedEl = Ext.DomHelper.append(field.getEl().dom.parentNode, { tag: 'img', alt: 'overridden', src: '${base}/images/overridden.gif', id: '${parameters.id}.overridden'}, true);
    inheritedEl.dom.qtip = 'overrides value defined by ${parameters.overriddenOwner}';
    field.getEl().addClass('field-overridden');
</#if>

<#assign helpkey>${parameters.name}.help</#assign>
<#assign helpmsg>${helpkey?i18n}</#assign>
<#if helpmsg?exists && helpkey != helpmsg>
    var xFormElement = field.getEl().findParent('.x-form-element')
    Ext.DomHelper.append(xFormElement, { tag: 'span', class: 'inline-help', html: '${helpmsg}', id:field.getId() + '-inline-help'});
</#if>
});

<#if parameters.scripts?exists>
    <#list parameters.scripts as script>
        <#include "/${script}.ftl"/>
    </#list>
</#if>