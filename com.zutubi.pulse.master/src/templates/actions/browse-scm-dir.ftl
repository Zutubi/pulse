(function(form, field)
{
    form.bind('action', function(e)
    {
        var fsw, project;

        if (e.field !== field || e.action !== 'browse') return;

        project = Zutubi.config.templateOwner(form.options.parentPath + "/" + form.options.baseName);
        fsw = new Zutubi.config.FileSystemWindow({
            title: 'select working directory',
            fs: 'scm',
            showFiles: false,
            basePath: project,
            targetField: field
        });

        fsw.show();
    });
});
