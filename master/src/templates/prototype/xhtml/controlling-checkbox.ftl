<#include "/prototype/xhtml/checkbox.ftl" />
(function()
{
    function shouldEnable(checkbox)
    {
        return <#if parameters.invert>!</#if>checkbox.getValue() && !checkbox.disabled;
    }

    <#include "/prototype/xhtml/controlling-field.ftl" />

    var checkbox = form.findField('${parameters.id}');
    checkbox.on('check', function(checkbox, checked) { setEnabledState(checkbox); });
    checkbox.on('disable', function(checkbox, checked) { setEnabledState(checkbox); });
    checkbox.on('enable', function(checkbox, checked) { setEnabledState(checkbox); });

    form.on('render', function() { setEnabledState(checkbox) });
}());
