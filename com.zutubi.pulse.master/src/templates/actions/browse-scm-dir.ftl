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

    var projectPath = findProjectPath('${field.parameters.path?js_string}');

<#assign title = "${field.name}" + ".popup.title"/>
    var browser = new ZUTUBI.WorkingCopyFileSystemBrowser({
        baseUrl : '${base}',
        showFiles: false,
        prefix:'scm',
        basePath: projectPath + '/scm/',
        title : '${title?i18n}',
        target : '${parameters.id?js_string}',
        onClose: function()
        {
            updateButtons();
        }
    });
    browser.show();
});
