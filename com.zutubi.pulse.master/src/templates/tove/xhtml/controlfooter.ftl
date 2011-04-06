${form.name}.items.last().on('render', function(field)
{
<#if parameters.actions?exists>
    <#list parameters.actions as action>
        <#assign i18nKey = "${parameters.name}.${action}"/>
        var linkEl = Ext.DomHelper.append(field.getEl().dom.parentNode, { tag: 'a', href: '#', html: '${i18nKey?i18n?js_string}', cls: 'field-action', id: '${parameters.id}.${action?js_string}'}, true);
        linkEl.on('click', function(e) { if(!linkEl.hasClass('x-item-disabled')) { field.fireEvent('${action?js_string}', field); } e.preventDefault(); });
    </#list>
</#if>

<#if parameters.overriddenValue?exists>
  <#if parameters.overriddenValue?is_sequence>
    var overriddenValue = [
    <#list parameters.overriddenValue as item>
        '${item?js_string}'<#if item_has_next>,</#if>
    </#list>
    ];
  <#else>
    var overriddenValue = '${parameters.overriddenValue?js_string}';
  </#if>
<#else>
    var overriddenValue = '';
</#if>
    addFieldAnnotations(${form.name}, field, ${parameters.required?default(false)?string}, ${parameters.noOverride?exists?string}, '${parameters.inheritedFrom!?js_string}', '${parameters.overriddenOwner!?js_string}', overriddenValue);

<#assign helpkey>${parameters.name}.help</#assign>
<#assign helpmsg>${helpkey?i18n}</#assign>
<#if helpmsg?exists && helpkey != helpmsg>
    addFieldHelp(${form.name}, field, '${helpmsg?js_string}');
<#elseif parameters.help?exists>
    addFieldHelp(${form.name}, field, '${parameters.help?js_string}');
</#if>
});

<#if parameters.scripts?exists>
    <#list parameters.scripts as script>
        <#include "/${script}.ftl"/>
    </#list>
</#if>
