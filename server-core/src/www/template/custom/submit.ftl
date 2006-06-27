<#if !parameters.cssClass?exists>
  <#assign parameters = parameters + {'cssClass': 'submit'}/>
</#if>
<#include "/${parameters.templateDir}/simple/submit.ftl" />
