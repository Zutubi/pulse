form.items.last().on('browse', function(field)
{
    var projectPath = '${field.parameters.parentPath}';
<#if !field.parameters.baseName?exists>
    projectPath = 'wizards/' + projectPath;
</#if>

    var prefix = 'scm';
<#if field.parameters.baseDirField?exists>
    var dirField = Ext.getCmp('zfid.${field.parameters.baseDirField}')
    prefix += '/' + dirField.getValue();
</#if>

    openSCMSelectDialog('${base}', true, '${form.name}', field.name, projectPath, prefix, '/');
});
