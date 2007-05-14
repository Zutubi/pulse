<#include "/prototype/xhtml/checkbox.ftl" />
(function()
{
    function setEnabledState(checkbox)
    {
        if(!form.rendered)
        {
            return;
        }

        var enabled = <#if parameters.invert>!</#if>checkbox.getValue();

    <#if parameters.dependentFields?exists && parameters.dependentFields?size &gt; 0>
        if(enabled)
        {
        <#list parameters.dependentFields as dependent>
            form.findField('${dependent}').enable();
        </#list>
        }
        else
        {
        <#list parameters.dependentFields as dependent>
            form.findField('${dependent}').disable();
        </#list>
        }
    <#else>
        form.items.each(function(field)
        {
            var type = field.getEl().dom.type;
            if(field.getId() != checkbox.getId() && type && type != "hidden" && type != "submit")
            {
                if(enabled)
                {
                    field.enable();
                }
                else
                {
                    field.clearInvalid();
                    field.disable();
                }
            }
        });
    </#if>
    }

    var checkbox = form.findField('${parameters.id}');
    checkbox.on('check', function(checkbox, checked) { setEnabledState(checkbox); });
    form.on('render', function() { setEnabledState(checkbox) });
}());