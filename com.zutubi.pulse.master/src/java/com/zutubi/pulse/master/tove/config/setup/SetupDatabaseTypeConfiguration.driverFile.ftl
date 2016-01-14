(function(form, field)
{
    form.bind('action', function(e)
    {
        var fsw;

        if (e.field !== field || e.action !== 'browse') return;

        fsw = new Zutubi.config.FileSystemWindow({
            title: 'select database driver',
            targetField: field
        });

        fsw.show();
    });
});
