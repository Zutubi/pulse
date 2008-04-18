    function setEnabledState(controllingField)
    {
<#if !form.readOnly>
        if(!form.rendered)
        {
            return;
        }

        var enabled = shouldEnable(controllingField)

    <#if parameters.dependentFields?exists && parameters.dependentFields?size &gt; 0>
        if(enabled)
        {
        <#list parameters.dependentFields as dependent>
            form.enableField('${dependent}');
        </#list>
        }
        else
        {
        <#list parameters.dependentFields as dependent>
            form.disableField('${dependent}');
        </#list>
        }
    <#else>
        form.items.each(function(field)
        {
            var type = field.getEl().dom.type;
            if(field.getId() != controllingField.getId() && type && type != "hidden" && type != "submit")
            {
                if(enabled)
                {
                    form.enableField(field.getId());
                }
                else
                {
                    form.disableField(field.getId());
                }
            }
        });
    </#if>
</#if>
    }
