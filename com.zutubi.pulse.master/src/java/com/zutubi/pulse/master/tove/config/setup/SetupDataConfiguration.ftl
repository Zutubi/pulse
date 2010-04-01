${form.name}.items.last().on('browse', function(field)
{
    var browser = new ZUTUBI.LocalFileSystemBrowser({
        baseUrl : '${base}',
        showFiles: false,
        title : '${"data.popup.title"?i18n}',
        target : '${parameters.id?js_string}'
    });
    browser.show(this);
});
