<#assign eKeys = fieldErrors.keySet()>
<#assign eKeysSize = eKeys.size()>
<#assign doneStartUlTag=false>
<#assign doneEndUlTag=false>
<#assign haveMatchedErrorField=false>
<#if (fieldErrorFieldNames?size > 0) >
	<#list fieldErrorFieldNames as fieldErrorFieldName>
		<#list eKeys as eKey>
		<#if (eKey = fieldErrorFieldName)>
			<#assign haveMatchedErrorField=true>
			<#assign eValue = fieldErrors[fieldErrorFieldName]>
			<#if (haveMatchedErrorField && (!doneStartUlTag))>
				<ul>
				<#assign doneStartUlTag=true>
			</#if>
			<#list eValue as eEachValue>
				<li><span class="errorMessage">${eEachValue}</span></li>
			</#list>			
		</#if>
		</#list>
	</#list>
	<#if (haveMatchedErrorField && (!doneEndUlTag))>
		</ul>
		<#assign doneEndUlTag=true>
	</#if>
<#else>	
	<#if (eKeysSize > 0)>
		<ul>
			<#list eKeys as eKey>
				<#assign eValue = fieldErrors[eKey]>
				<#list eValue as eEachValue>
					<li><span class="errorMessage">${eEachValue}</span></li>
				</#list>
			</#list>
		</ul>
	</#if>
</#if>

