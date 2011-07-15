<#include "/tove/xhtml/controlheader.ftl" />

(function()
{
<#if parameters.value?exists && parameters.value == "true">
    fc.checked = true;
</#if>
    fc.width = 14;
    fc.autoCreate = { tag: 'input', type: 'checkbox', value: 'true', id: fc.id };

    ${form.name}.add(new Ext.form.Hidden({name: '${parameters.name}__default', value: 'false'}));

    var checkbox = new Ext.form.Checkbox(fc);
    ${form.name}.add(checkbox);
    checkbox.on('check', updateButtons);
}());

<#include "/tove/xhtml/controlfooter.ftl" />