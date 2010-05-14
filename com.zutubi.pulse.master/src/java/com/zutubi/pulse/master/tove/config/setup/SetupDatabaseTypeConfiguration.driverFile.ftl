${form.name}.items.last().on('browse', function(field)
{
    var browser = new ZUTUBI.LocalFileSystemBrowser({
        baseUrl : '${base}',
        isWindows: ${isWindows},
        title : '${"driverFile.popup.title"?i18n}',
        target : '${parameters.id?js_string}'
    });
    browser.show();
});
