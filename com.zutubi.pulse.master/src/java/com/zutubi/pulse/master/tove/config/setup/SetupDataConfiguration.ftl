${form.name}.items.last().on('browse', function(field)
{
    var browser = new ZUTUBI.LocalFileSystemBrowser({
        baseUrl : '${base}',
        showFiles: false,
        title : 'select data directory',
        target : '${parameters.id?js_string}'
    });
    browser.show(this);
});
