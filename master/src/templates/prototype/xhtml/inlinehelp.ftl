<#assign helpkey>${parameters.name}.help</#assign>
<#assign helpmsg>${helpkey?i18n}</#assign>
<#if helpmsg?exists && helpkey != helpmsg>
form.items.last().on('render', function(field) { Ext.DomHelper.append(field.getEl().dom.parentNode, { tag: 'span', class: 'inline-help', html: '${helpmsg}'}) });
</#if>
