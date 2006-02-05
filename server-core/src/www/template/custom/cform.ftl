<#include "/${parameters.templateDir}/custom/form-validate.ftl" />
<#include "/${parameters.templateDir}/simple/form.ftl" />
<table class="form">
<#if parameters.heading?exists>
   <tr>
        <th class="heading" colspan="2">${parameters.heading?html}</th>
   </tr>
</#if>
