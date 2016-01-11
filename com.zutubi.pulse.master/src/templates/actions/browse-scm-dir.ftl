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
        var browser = new Zutubi.fs.WorkingCopyFileSystemBrowser({
            baseUrl : window.baseUrl,
            showFiles: false,
            prefix: 'scm',
            basePath: projectPath + '/scm/',
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
