${form.name}.items.last().on('browse', function(field)
{
    var browser = new ZUTUBI.LocalFileSystemBrowser({
        baseUrl : '${base}',
        title : '${"plugin.popup.title"?i18n}',
        target : '${parameters.id?js_string}'
    });
    browser.show();
});