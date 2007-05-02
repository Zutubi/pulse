<#include "/prototype/xhtml/checkbox.ftl" />
<script type="text/javascript">
    function setEnabledState_${parameters.id}(checkbox)
    {
        var disabled = <#if !parameters.invert>!</#if>checkbox.checked;
<#list parameters.dependentFields as dependent>
        Ext.get("${dependent}").dom.disabled = disabled;
</#list>
    }

    Ext.get("${parameters.id}").on("click", function(event) { setEnabledState_${parameters.id}(event.getTarget()); });
    Ext.onReady(function() { setEnabledState_${parameters.id}(Ext.get("${parameters.id}").dom) });
</script>