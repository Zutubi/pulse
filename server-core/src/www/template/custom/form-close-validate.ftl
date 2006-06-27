<#--
START SNIPPET: supported-validators
Only the following validators are supported:
* required validator
* requiredstring validator
* email validator
* url validator
* int validator
END SNIPPET: supported-validators
-->
<#if parameters.validate?exists>
<script>
    function validateForm_${parameters.id}() {
        form = document.getElementById("${parameters.id}");
        clearErrorMessages(form);
        clearErrorLabels(form);

        var errors = false;
    <#list parameters.tagNames as tagName>
        <#list tag.getValidators("${tagName}") as validator>
        // field name: ${validator.fieldName}
        // validator name: ${validator.validatorType}
        if (form.elements['${validator.fieldName}']) {
            field = form.elements['${validator.fieldName}'];
            var error = "${validator.defaultMessage}";
            <#if validator.validatorType = "required">
            if (field.value == "") {
                addError(field, error);
                errors = true;
            }
            <#elseif validator.validatorType = "requiredstring">
            if (field.value != null && (field.value == "" || field.value.match("\W+"))) {
                addError(field, error);
                errors = true;
            }
            <#elseif validator.validatorType = "email">
            if (field.value != null && field.value.match(/\\b(^(\\S+@).+((\\.com)|(\\.net)|(\\.org)|(\\.info)|(\\.edu)|(\\.mil)|(\\.gov)|(\\.biz)|(\\.ws)|(\\.us)|(\\.tv)|(\\.cc)|(\\..{2,2}))$)\\b/gi)) {
                addError(field, error);
                errors = true;
            }
            <#elseif validator.validatorType = "url">
            if (field.value != null && field.value.match(/^(file|http):\\/\\/\\S+\\.(com|net|org|info|edu|mil|gov|biz|ws|us|tv|cc)$/i)) {
                addError(field, error);
                errors = true;
            }
            <#elseif validator.validatorType = "int">
            if (field.value != null && (parseInt(field.value) < ${validator.min} || parseInt(field.value) > ${validator.max})) {
                addError(field, error);
                errors = true;
            }
            </#if>
        }
        </#list>
    </#list>

        return !errors;
    }
</script>
</#if>