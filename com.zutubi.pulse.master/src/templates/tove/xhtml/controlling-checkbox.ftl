<#include "/tove/xhtml/checkbox.ftl" />
(function()
{
    function shouldEnable(checkbox)
    {
        return <#if parameters.invert>!</#if>checkbox.getValue() && !checkbox.disabled;
    }

    <#include "/tove/xhtml/controlling-field.ftl" />

    var checkbox = ${form.name}.findById('${parameters.id}');
    checkbox.on('check', function(checkbox, checked) { setEnabledState(checkbox); });
    checkbox.on('disable', function(checkbox, checked) { setEnabledState(checkbox); });
    checkbox.on('enable', function(checkbox, checked) { setEnabledState(checkbox); });

    ${form.name}.on('afterlayout', function() { setEnabledState(checkbox); }, ${form.name}, {single: true});
}());
