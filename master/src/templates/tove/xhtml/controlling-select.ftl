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

    var select = form.findById('${parameters.id}');
    select.on('select', setEnabledState);
    select.on('disable', setEnabledState);
    select.on('enable', setEnabledState);

    form.on('afterlayout', function() { setEnabledState(select) }, form, {single: true});
}());
