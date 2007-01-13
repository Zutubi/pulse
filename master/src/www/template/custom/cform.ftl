<#include "/${parameters.templateDir}/custom/form-validate.ftl" />
<#include "/${parameters.templateDir}/simple/form.ftl" />
<table class="form"
<#if parameters.width?exists>
       width="${parameters.width}"
</#if>
>
<#if parameters.heading?exists>
   <tr>
        <th class="heading" colspan="${parameters.colspan?default('2')}">
            ${parameters.heading?html}
        </th>
   </tr>
</#if>
