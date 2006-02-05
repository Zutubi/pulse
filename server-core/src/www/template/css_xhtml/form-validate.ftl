<#if parameters.validate?default(false) == true>
<script src="${base}/webwork/css_xhtml/validation.js"></script>
    <#if parameters.onsubmit?exists>
        ${tag.addParameter('onsubmit', "${parameters.onsubmit}; return validateForm_${parameters.id}();")}
    <#else>
        ${tag.addParameter('onsubmit', "return validateForm_${parameters.id}();")}
    </#if>
</#if>
