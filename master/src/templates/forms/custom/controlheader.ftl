<#include "/${parameters.templateDir}/custom/controlheader-core.ftl" />
    <td <#rt/>
<#if hasFieldErrors>
        class="error-field"<#t/>
<#else>
        class="field"<#t/>
</#if>
    ><#t/>
