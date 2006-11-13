<#if !stack.findValue("#datepicker_js_included")?exists>
<#assign trash = stack.setValue("#datepicker_js_included", true)/>
<script type="text/javascript" src="<@ww.url value="/webwork/jscalendar/"/>calendar.js"></script>
<script type="text/javascript" src="<@ww.url value="/webwork/jscalendar/lang/"/>calendar-${parameters.language?default("en")}.js"></script>
<script type="text/javascript" src="<@ww.url value="/webwork/jscalendar/"/>calendar-setup.js"></script>
</#if>
<#include "/${parameters.templateDir}/simple/text.ftl" />
<a href="#" id="${parameters.id}_button"><img src="<@ww.url value="/webwork/jscalendar/img.gif"/>" width="16" height="16" border="0" alt="Click Here to Pick up the date"></a>
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "${parameters.id}",
<#if parameters.format?exists>
        ifFormat       :    "${parameters.format}",
</#if>
<#if parameters.showstime?exists>
        showsTime      :    "${parameters.showstime}",
</#if>
        button         :    "${parameters.id}_button",
<#if parameters.singleclick?exists>
        singleclick    :    ${parameters.singleclick},
</#if>
        step           :    1
    });
</script>
