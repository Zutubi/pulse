<#if parameters.browseLink?exists>
form.items.last().on('render', function(field)
{
    var linkEl = field.getEl().insertSibling({ tag: 'a', href: '#', html: '${parameters.browseLink?i18n}', class: 'browse', id: '${parameters.id}.${parameters.browseLink}' }, 'after');
    linkEl.on('click', function(e) { field.fireEvent('browse', field); e.preventDefault(); });
});
</#if>
<#if parameters.scripts?exists>
    <#list parameters.scripts as script>
        <#include "/${script}.ftl"/>
    </#list>
</#if>