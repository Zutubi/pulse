[#ftl]

<#---------------------------------------------------------------------------
A macro to show the messages directly on the result object of the given
level.
---------------------------------------------------------------------------->
[#macro resultMessages result level indent=""]
    [#if level == errorLevel]
        [#if result.errorMessage?exists]
${indent}  * ${renderer.wrapString(result.errorMessage, "${indent}    ")}
        [/#if]
        [#if result.failureMessage?exists]
${indent}  * ${renderer.wrapString(result.failureMessage, "${indent}    ")}
        [/#if]
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
Returns true iff the given result has messages of the given level directly on
it.
---------------------------------------------------------------------------->
[#function hasDirectMessages result level]
    [#if level == errorLevel]
        [#return result.errorMessage?exists || result.failureMessage?exists]
    [#else]
        [#return false]
    [/#if]
[/#function]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the file artifact as a flat list but
with context.
---------------------------------------------------------------------------->
[#macro fileArtifactMessages artifact level context]
    [#if artifact.hasMessages(level)]
  - ${renderer.wrapString("${context}", "    ")}
    ${renderer.wrapString("${artifact.path}", "    ")}
        [#list artifact.getFeatures(level) as feature]
    * ${renderer.wrapString(feature.summary, "      ")}
        [/#list]
    [/#if]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the artifact as a flat list but with
context.
---------------------------------------------------------------------------->
[#macro artifactMessages artifact level context]
    [#assign fileContext="${context} :: ${artifact.name}"]
    [#list artifact.children as fileArtifact]
        [@fileArtifactMessages artifact=fileArtifact level=level context=fileContext/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the command result and its included
artifacts as a flat list but with context.
---------------------------------------------------------------------------->
[#macro commandResultMessages result level context]
    [#assign nestedContext = "${context} :: ${result.commandName}"]
    [#if hasDirectMessages(result, level)]
  - ${renderer.wrapString(nestedContext, "    ")}
        [@resultMessages result=result level=level indent="  "/]
    [/#if]
    [#list result.artifacts as artifact]
        [@artifactMessages artifact=artifact level=level context=nestedContext/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the recipe result and its included
results as a flat list but with context.
---------------------------------------------------------------------------->
[#macro recipeResultMessages result level context]
    [#if hasDirectMessages(result, level)]
  - ${renderer.wrapString(context, "    ")}
        [@resultMessages result=result level=level indent="  "/]
    [/#if]
    [#list result.commandResults as commandResult]
        [@commandResultMessages result=commandResult level=level context=context/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the result node and its included
results as a flat list but with context.
---------------------------------------------------------------------------->
[#macro recipeNodeMessages node level context=""]
    [#if context?length &gt; 0]
        [#assign nestedContext = "${context} :: ${node.result.recipeNameSafe}"]
    [#else]
        [#assign nestedContext = node.result.recipeNameSafe]
    [/#if]
    [@recipeResultMessages result=node.result level=level context=nestedContext/]
    [#list node.children as child]
        [@recipeNodeMessages node=child level=level context=nestedContext/]
    [/#list]
[/#macro]

<#---------------------------------------------------------------------------
Shows all messages of the given level on the build result and its included
results as a flat list but with context.
---------------------------------------------------------------------------->
[#macro buildMessages result level]
    [#if result.hasMessages(level)]
${level?lower_case?cap_first} messages:
        [@resultMessages result=result level=level/]
        [#list result.root.children as child]
            [@recipeNodeMessages node=child level=level/]
        [/#list]
    [/#if]
[/#macro]
