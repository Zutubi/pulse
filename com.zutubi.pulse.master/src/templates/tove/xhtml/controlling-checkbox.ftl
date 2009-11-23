<#include "/tove/xhtml/checkbox.ftl" />
(function()
{
    function isChecked(checkbox)
    {
        return checkbox.getValue() && !checkbox.disabled;
    }

    function setEnabledState(checkbox)
    {
<#if !form.readOnly>
        if(!${form.name}.rendered)
        {
            return;
        }

        var checked = isChecked(checkbox);

    <#if parameters.checkedFields?exists && parameters.checkedFields?size &gt; 0 || parameters.uncheckedFields?exists && parameters.uncheckedFields?size &gt; 0>
        <#if parameters.checkedFields?exists>
            <#list parameters.checkedFields as field>
        if (checked)
        {
            ${form.name}.enableField('zfid.${field}');
        }
        else
        {
            ${form.name}.disableField('zfid.${field}');
        }
            </#list>
        </#if>
        <#if parameters.uncheckedFields?exists>
            <#list parameters.uncheckedFields as field>
        if (checked)
        {
            ${form.name}.disableField('zfid.${field}');
        }
        else
        {
            ${form.name}.enableField('zfid.${field}');
        }
            </#list>
        </#if>
    <#else>
        ${form.name}.items.each(function(field)
        {
            var type = field.getEl().dom.type || 'default';
            if(field.getId() != checkbox.getId() && type != 'hidden' && type != 'submit')
            {
                if(checked)
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

    var checkbox = ${form.name}.findById('${parameters.id}');
    checkbox.on('check', function(checkbox, checked) { setEnabledState(checkbox); });
    checkbox.on('disable', function(checkbox, checked) { setEnabledState(checkbox); });
    checkbox.on('enable', function(checkbox, checked) { setEnabledState(checkbox); });

    ${form.name}.on('afterlayout', function() { setEnabledState(checkbox); }, ${form.name}, {single: true});
}());
