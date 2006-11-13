<#if !stack.findValue("#optiontransferselect_js_included")?exists>
	<script language="javascript" src="<@ww.url value="/webwork/optiontransferselect/optiontransferselect.js" />"></script>
	<#assign temporaryVariable = stack.setValue("#optiontransferselect_js_included", "true") />
</#if>
<TABLE BORDER="0">
<TR>
<TD>
<#if parameters.leftTitle?exists>
	<label for="leftTitle">${parameters.leftTitle}</label><BR/>
</#if>
<#include "/${parameters.templateDir}/simple/select.ftl" /> </TD>
<TD VALIGN="MIDDLE" ALIGN="CENTER">
	<#if parameters.allowAddToLeft?default(true)>
		<#assign addToLeftLabel = parameters.addToLeftLabel?default("<-")?html/>
		<#if parameters.doubleHeaderKey?exists>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${addToLeftLabel}" ONCLICK="moveSelectedOptions(document.getElementById('${parameters.doubleId?html}'), document.getElementById('${parameters.id?html}'), false, '${parameters.doubleHeaderKey}', '')" /><BR/><BR/>
		<#else>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${addToLeftLabel}" ONCLICK="moveSelectedOptions(document.getElementById('${parameters.doubleId?html}'), document.getElementById('${parameters.id?html}'), false, '')" /><BR/><BR/>
		</#if>
	</#if>
	<#if parameters.allowAddToRight?default(true)>
		<#assign addToRightLabel=parameters.addToRightLabel?default("->")?html />
		<#if parameters.headerKey?exists>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${addToRightLabel}" ONCLICK="moveSelectedOptions(document.getElementById('${parameters.id?html}'), document.getElementById('${parameters.doubleId?html}'), false, '${parameters.headerKey}', '')" /><BR/><BR/>
		<#else>
			<INPUT TYPE="button"
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${addToRightLabel}" ONCLICK="moveSelectedOptions(document.getElementById('${parameters.id?html}'), document.getElementById('${parameters.doubleId?html}'), false, '')" /><BR/><BR/>
		</#if>
	</#if>
	<#if parameters.allowAddAllToLeft?default(true)>
		<#assign addAllToLeftLabel=parameters.addAllToLeftLabel?default("<<--")?html />
		<#if parameters.doubleHeaderKey?exists>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass}"
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle}"
			</#if>
			 VALUE="${addAllToLeftLabel}" ONCLICK="moveAllOptions(document.getElementById('${parameters.doubleId?html}'), document.getElementById('${parameters.id?html}'), false, '${parameters.doubleHeaderKey}', '')" /><BR/><BR/>
		<#else>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${addAllToLeftLabel}" ONCLICK="moveAllOptions(document.getElementById('${parameters.doubleId?html}'), document.getElementById('${parameters.id?html}'), false, '')" /><BR/><BR/>
		</#if>
	</#if>
	<#if parameters.allowAddAllToRight?default(true)>
		<#assign addAllToRightLabel=parameters.addAllToRightLabel?default("-->>")?html />
		<#if parameters.headerKey?exists>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${addAllToRightLabel}" ONCLICK="moveAllOptions(document.getElementById('${parameters.id?html}'), document.getElementById('${parameters.doubleId?html}'), false, '${parameters.headerKey}', '')" /><BR/><BR/>	
		<#else>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${addAllToRightLabel}" ONCLICK="moveAllOptions(document.getElementById('${parameters.id?html}'), document.getElementById('${parameters.doubleId?html}'), false, '')" /><BR/><BR/>	
		</#if>
	</#if>
	<#if parameters.allowSelectAll?default(true)>
		<#assign selectAllLabel=parameters.selectAllLabel?default("<*>")?html />
		<#if parameters.headerKey?exists && parameters.doubleHeaderKey?exists>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${selectAllLabel}" ONCLICK="selectAllOptionsExceptSome(document.getElementById('${parameters.id?html}'), 'key', '${parameters.headerKey}');selectAllOptionsExceptSome(document.getElementById('${parameters.doubleId?html}'), 'key', '${parameters.doubleHeaderKey}');" /><BR/><BR/>
		<#elseif parameters.headerKey?exists>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${selectAllLabel}" ONCLICK="selectAllOptionsExceptSome(document.getElementById('${parameters.id?html}'), 'key', '${parameters.headerKey}');selectAllOptions(document.getElementById('${parameters.doubleId?html}'));" /><BR/><BR/>
		<#elseif parameters.doubleHeaderKey?exists>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${selectAllLabel}" ONCLICK="selectAllOptions(document.getElementById('${parameters.id?html}'));selectAllOptionsExceptSome(document.getElementById('${parameters.doubleId?html}'), 'key', '${parameters.doubleHeaderKey}');" /><BR/><BR/>
		<#else>
			<INPUT TYPE="button" 
			<#if parameters.buttonCssClass?exists>
			 CLASS="${parameters.buttonCssClass?html}" 
			</#if>
			<#if parameters.buttonCssStyle?exists>
			 STYLE="${parameters.buttonCssStyle?html}"
			</#if>
			 VALUE="${selectAllLabel}" ONCLICK="selectAllOptions(document.getElementById('${parameters.id?html}'));selectAllOptions(document.getElementById('${parameters.doubleId?html}'));" /><BR/><BR/>
		</#if>
		
	</#if>
</TD>
<TD>
<#if parameters.rightTitle?exists>
	<label for="rightTitle">${parameters.rightTitle}</label><BR/>
</#if>
<select 
	name="${parameters.doubleName?default("")?html}"
	<#if parameters.get("doubleSize")?exists>
	size="${parameters.get("size")?html}"		
	</#if>
	<#if parameters.doubleDisabled?default(false)>
	disabled="disabled"
	</#if>
	<#if parameters.doubleMultiple?exists>
	multiple="multiple"
	</#if>
	<#if parameters.doubleTabindex?exists>
	tabindex="${parameters.tabindex?html}"
	</#if>
	<#if parameters.doubleId?exists>
	id="${parameters.doubleId?html}"
	</#if>
	<#if parameters.doubleCssClass?exists>
	class="${parameters.cssClass?html}"
	</#if>
	<#if parameters.doubleCssStyle?exists>
	style="${parameters.cssStyle?html}"
	</#if>
    <#if parameters.doubleOnclick?exists>
    onclick="${parameters.doubleOnclick?html}"
    </#if>
    <#if parameters.doubleOndblclick?exists>
    ondblclick="${parameters.doubleOndblclick?html}"
    </#if>
    <#if parameters.doubleOnmousedown?exists>
    onmousedown="${parameters.doubleOnmousedown?html}"
    </#if>
    <#if parameters.doubleOnmouseup?exists>
    onmouseup="${parameters.doubleMnmouseup?html}"
    </#if>
    <#if parameters.doubleOnmousemove?exists>
    onmousemove="${parameters.doubleOnmousemove?html}"
    </#if>
    <#if parameters.doubleOnmouseout?exists>
    onmouseout="${parameters.doubleOnmouseout?html}"
    </#if>
    <#if parameters.doubleOnfocus?exists>
    onfocus="${parameters.doubleOnfocus?html}"
    </#if>
    <#if parameters.doubleOnblur?exists>
    onblur="${parameters.doubleOnblur?html}"
    </#if>
    <#if parameters.doubleOnkeypress?exists>
    onkeypress="${parameters.doubleOnkeypress?html}"
    </#if>
    <#if parameters.doubleOnKeydown?exists>
    onkeydown="${parameters.doubleOnkeydown?html}"
    </#if>
    <#if parameters.doubleOnkeyup?exists>
    onkeyup="${parameters.doubleOnkeyup?html}"
    </#if>
    <#if parameters.doubleOnselect?exists>
    onselect="${parameters.doubleOnselect?html}"
    </#if>
    <#if parameters.doubleOnchange?exists>
    onchange="${parameters.doubleOnchange?html}"
    </#if>
>
	<#if parameters.doubleHeaderKey?exists && parameters.doubleHeaderValue?exists>
    <option value="${parameters.doubleHeaderKey?html}">${parameters.doubleHeaderValue?html}</option>
	</#if>
	<#if parameters.doubleEmptyOption?default(false)>
    <option value=""></option>
	</#if>
	<@ww.iterator value="parameters.doubleList">
        <#if parameters.doubleListKey?exists>
            <#assign doubleItemKey = stack.findValue(parameters.doubleListKey) />
        <#else>
            <#assign doubleItemKey = stack.findValue('top') />
        </#if>
        <#assign doubleItemKeyStr = doubleItemKey.toString() />
        <#if parameters.listValue?exists>
            <#assign doubleItemValue = stack.findString(parameters.doubleListValue) />
        <#else>
            <#assign doubleItemValue = stack.findString('top') />
        </#if>
    	<option value="${doubleItemKeyStr?html}"<#rt/>
        <#if tag.contains(parameters.doubleNameValue, doubleItemKey)>
 		selected="selected"<#rt/>
        </#if>
    	>${doubleItemValue?html}</option><#lt/>
	</@ww.iterator>
</select> </TD>
</TR>
<TABLE>

