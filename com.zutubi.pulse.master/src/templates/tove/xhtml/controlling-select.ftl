<#include "/tove/xhtml/combobox.ftl" />
(function()
{
    function shouldEnable(select)
    {
        <#if parameters.enableSet?exists>
            var selected = select.getValue();
            <#list parameters.enableSet as value>
            if(selected == '${value}')
            {
                return true;
            }
            </#list>
        </#if>

        return false;
    }

    <#include "/tove/xhtml/controlling-field.ftl" />

    var select = ${form.name}.findById('${parameters.id}');
    select.on('select', setEnabledState);
    select.on('disable', setEnabledState);
    select.on('enable', setEnabledState);

    ${form.name}.on('afterlayout', function() { setEnabledState(select); }, ${form.name}, {single: true});
}());
