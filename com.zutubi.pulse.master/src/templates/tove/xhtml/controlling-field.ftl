    function setEnabledState(controllingField)
    {
<#if !form.readOnly>
        if(!${form.name}.rendered)
        {
            return;
        }

        var enabled = shouldEnable(controllingField);

    <#if parameters.dependentFields?exists && parameters.dependentFields?size &gt; 0>
        if(enabled)
        {
        <#list parameters.dependentFields as dependent>
            ${form.name}.enableField('zfid.${dependent}');
        </#list>
        }
        else
        {
        <#list parameters.dependentFields as dependent>
            ${form.name}.disableField('zfid.${dependent}');
        </#list>
        }
    <#else>
        ${form.name}.items.each(function(field)
        {
            var type = field.getEl().dom.type;
            if(field.getId() != controllingField.getId() && type && type != "hidden" && type != "submit")
            {
                if(enabled)
                {
                    ${form.name}.enableField(field.getId());
                }
                else
                {
                    ${form.name}.disableField(field.getId());
                }
            }
        });
    </#if>
</#if>
    }
