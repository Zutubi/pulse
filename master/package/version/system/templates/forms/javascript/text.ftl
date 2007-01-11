// The field.
var field_${parameters.id?html} = form.elements['${parameters.id?html}'];
<#if parameters.required?default(false)>
Element.addClassName(field_${parameters.id?html}, 'required');
</#if>