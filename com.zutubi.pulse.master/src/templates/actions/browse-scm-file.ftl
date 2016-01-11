(function(form, field)
{
    form.bind('action', function(e)
    {
        if (e.field !== field || e.action !== 'browse') return;

        function findProjectPath(path)
        {
            var parentPath = Zutubi.config.parentPath(path);
            while(parentPath != null && parentPath != 'projects')
            {
                path = parentPath;
                parentPath = Zutubi.config.parentPath(path);
            }

            return 'c' + path;
        }

        var projectPath = findProjectPath(form.options.parentPath + "/" + form.options.baseName);
        var prefix = '';
<#if field.parameters.baseDirField?exists>
        var dirField = form.getFieldNamed('${field.parameters.baseDirField}');
        prefix += dirField.getValue();
</#if>

        var browser = new Zutubi.fs.WorkingCopyFileSystemBrowser({
            baseUrl : window.baseUrl,
            showFiles: true,
            prefix: prefix,
            basePath: projectPath + '/scm/' + prefix,
            title : 'browse',
            target : field.element.get().id,
            onClose: function()
            {
                form._updateButtons();
            }
        });
        browser.show();
    });
});
