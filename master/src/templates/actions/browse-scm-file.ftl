form.items.last().on('browse', function(field)
{
    function getParentPath(path)
    {
        var index = path.lastIndexOf('/');
        if(index >= 0)
        {
            return path.slice(0, index);
        }

        return null;
    }

    function findProjectPath(path)
    {
        var parentPath = getParentPath(path);
        while(parentPath != null && parentPath != 'projects')
        {
            path = getParentPath(path);
            parentPath = getParentPath(path);
        }
        
        return path;
    }

    var projectPath;
<#if field.parameters.baseName?exists>
     projectPath = 'c' + findProjectPath('${field.parameters.parentPath}/${field.parameters.baseName}');
<#else>
     projectPath = 'wizards/${field.parameters.parentPath}';
</#if>

    var prefix = 'scm';
<#if field.parameters.baseDirField?exists>
    var dirField = Ext.getCmp('zfid.${field.parameters.baseDirField}')
    prefix += '/' + dirField.getValue();
</#if>

    openSCMSelectDialog('${base}', true, '${form.name}', field.name, projectPath, prefix, '/');
});
