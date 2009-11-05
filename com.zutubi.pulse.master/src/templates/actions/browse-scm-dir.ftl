${form.name}.items.last().on('browse', function(field)
{
    function findProjectPath(path)
    {
<#if field.parameters.baseName?exists>
        var start = 'c';
<#else>
        var start = 'wizards/';
</#if>
        var parentPath = getParentPath(path);
        while(parentPath != null && parentPath != 'projects')
        {
            path = parentPath;
            parentPath = getParentPath(path);
            start = '';
        }

        return start + path;
    }

    var projectPath = findProjectPath('${field.parameters.parentPath?js_string}');
    openSCMSelectDialog('${base}', false, '${form.name}', field.name, projectPath, '', 'scm');
});
