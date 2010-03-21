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

    var browser = new ZUTUBI.PulseFileSystemBrowser({
        baseUrl : '${base}',
        showFiles: false,
        prefix:'scm',
        basePath: projectPath + '/scm/',
        title : 'select directory',
        target : '${parameters.id?js_string}'
    });
    browser.show(this);
});
