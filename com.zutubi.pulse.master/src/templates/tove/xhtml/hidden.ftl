<#include "/tove/xhtml/controlheader.ftl" />

<#if parameters.value?exists>
    fc.value = '${parameters.value?js_string}';
</#if>
form.add(new Ext.form.Hidden(fc));

<#include "/tove/xhtml/controlfooter.ftl" />
