(function(form, field)
{
    form.bind('action', function(e)
    {
        if (e.field !== field || e.action !== 'browse') return;

        var browser = new Zutubi.fs.LocalFileSystemBrowser({
            baseUrl : window.baseUrl,
            isWindows: ${isWindows},
            title : 'select database driver',
            target : field.element.get().id
        });
        browser.show();
    });
});
