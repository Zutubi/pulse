(function(form, field)
{
    form.bind('action', function(e)
    {
        if (e.field !== field || e.action !== 'browse') return;

        var browser = new Zutubi.fs.LocalFileSystemBrowser({
            baseUrl : window.baseUrl,
            showFiles: false,
            isWindows: ${isWindows},
            title : 'select data directory',
            target : field.element.get().id
        });
        browser.show();
    });
});
