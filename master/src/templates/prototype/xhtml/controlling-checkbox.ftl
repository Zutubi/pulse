<#include "/prototype/xhtml/checkbox.ftl" />
(function()
{
    function _enable(form, field)
    {
        field.enable();

        <#-- lookup and enable the fields label and inline help text. -->
        var labelDomEl = Ext.query("//[@for='"+field.getId()+"']", form.el.dom)[0];
        if (labelDomEl)
        {
            Ext.get(labelDomEl).removeClass('x-item-disabled');
        }
        var helpDomEl = Ext.query("//[@id='"+field.getId()+"-inline-help']", form.el.dom)[0];
        if (helpDomEl)
        {
            Ext.get(helpDomEl).removeClass('x-item-disabled');
        }
    }

    function _disable(form, field)
    {
        field.clearInvalid();
        field.disable();

        var labelDomEl = Ext.query("//[@for='"+field.getId()+"']", form.el.dom)[0];
        if (labelDomEl)
        {
            Ext.get(labelDomEl).addClass('x-item-disabled');
        }
        var helpDomEl = Ext.query("//[@id='"+field.getId()+"-inline-help']", form.el.dom)[0];
        if (helpDomEl)
        {
            Ext.get(helpDomEl).addClass('x-item-disabled');
        }
    }


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
            _enable(form, form.findField('${dependent}'));
        </#list>
        }
        else
        {
        <#list parameters.dependentFields as dependent>
            _disable(form, form.findField('${dependent}'));
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
                    _enable(form, field);
                }
                else
                {
                    _disable(form, field);
                }
            }
        });
    </#if>
    }

    var checkbox = form.findField('${parameters.id}');
    checkbox.on('check', function(checkbox, checked) { setEnabledState(checkbox); });
    form.on('render', function() { setEnabledState(checkbox) });
}());