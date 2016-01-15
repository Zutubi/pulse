(function(form, field)
{
    form.bind('action', function(e)
    {
        var rw;

        if (e.field !== field || e.action !== 'browse') return;

        rw = new Zutubi.admin.ResourceBrowserWindow({
            resourceField: field,
            defaultVersionField: form.getFieldNamed("defaultVersion"),
            versionField: form.getFieldNamed("version")
        });

        rw.show();
    });
});
