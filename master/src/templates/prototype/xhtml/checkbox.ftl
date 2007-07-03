<#include "/prototype/xhtml/controlheader.ftl" />

<#if parameters.value?exists && parameters.value == "true">
    fieldConfig.checked = true;
</#if>
fieldConfig.autoCreate = { tag: 'input', type: 'checkbox', value: 'true', id: fieldConfig.id };
form.add(new Ext.form.Checkbox(fieldConfig));
hiddenFields.push({name: '${parameters.name}.default', value: 'false'});

<#include "/prototype/xhtml/controlfooter.ftl" />