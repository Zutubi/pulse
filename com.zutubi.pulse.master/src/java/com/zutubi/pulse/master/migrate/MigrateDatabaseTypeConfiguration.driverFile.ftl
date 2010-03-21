${form.name}.items.last().on('browse', function(field)
{
    var browser = new ZUTUBI.LocalFileSystemBrowser({
        baseUrl : '${base}',
        title : 'select database driver',
        target : '${parameters.id?js_string}'
    });
    browser.show(this);
});