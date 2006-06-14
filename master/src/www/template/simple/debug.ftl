<script language="JavaScript" type="text/javascript">
<!--
    function toggleDebug(debugId) {
        var debugDiv = document.getElementById(debugId);
        if (debugDiv) {
            var display = debugDiv.style.display;
            if (display == 'none') {
                debugDiv.style.display = 'block';
            } else if (display == 'block') {
                debugDiv.style.display = 'none';
            }
        }
    }
-->
</script>
<p/>
<a href="#" onclick="toggleDebug('${parameters.id?default("debug")}')">[Debug]</a>
<div style="display:none" id="${parameters.id?default("debug")}">
<h2>WebWork ValueStack Debug</h2>
<p/>
<h3>Stack Context</h3>
<i>These items are available using the #key notation</i>
<table border="0" cellpadding="5" cellspacing="0" width="100%" bgcolor="#DDDDDD">
<tr>
<th>Key</th><th>Value</th>
</tr>
#set ($index = 1)
#foreach ($contextKey in $stack.context.keySet())
<tr bgcolor="#if (($index % 2) == 0)#BBBBBB#else#CCCCCC#end">
<td>$contextKey</td><td>$stack.context.get($contextKey)</td>
</tr>
#set ($index = $index + 1)
#end
</table>
<p/>
<h3>Value Stack Contents</h3>
#set ($stackContents = $parameters.stackValues)
<table border="0" cellpadding="5" cellspacing="0" width="100%" bgcolor="#DDDDDD">
<tr><th>Object</th><th>Property Name</th><th>Property Value</th></tr>
#set ($index = 1)
#foreach ($stackObject in $stackContents)
<tr>
<td rowspan="$stackObject.value.size()">$stackObject.key</td>
#set ($renderRow = false)
#set ($propertyMap = $stackObject.value)
#foreach ($propertyName in $propertyMap.keySet())
#if ($renderRow == true)<tr>#else #set ($renderRow = true) #end
<td bgcolor="#if (($index % 2) == 0)#BBBBBB#else#CCCCCC#end">$propertyName</td>
<td bgcolor="#if (($index % 2) == 0)#BBBBBB#else#CCCCCC#end">$!propertyMap.get($propertyName)</td>
</tr>
#set ($index = $index + 1)
#end
#end
</table>
</div>