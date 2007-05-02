<#include "/prototype/xhtml/checkbox.ftl" />
<script type="text/javascript">
    function setEnabledState_${parameters.id}(checkbox)
    {
        var disabled = <#if !parameters.invert>!</#if>checkbox.checked;

<#if parameters.dependentFields?exists && parameters.dependentFields?size &gt; 0>
    <#list parameters.dependentFields as dependent>
        Ext.get("${dependent}").dom.disabled = disabled;
    </#list>
<#else>
        var fields = checkbox.form.elements;
        for(var i = 0; i < fields.length; i++)
        {
            var field = fields[i];
            if(field.id != checkbox.id && field.type && field.type != "hidden" && field.type != "submit")
            {
                fields[i].disabled = disabled;
            }
        }
</#if>
    }

    Ext.get("${parameters.id}").on("click", function(event) { setEnabledState_${parameters.id}(event.getTarget()); });
    Ext.onReady(function() { setEnabledState_${parameters.id}(Ext.get("${parameters.id}").dom) });
</script>