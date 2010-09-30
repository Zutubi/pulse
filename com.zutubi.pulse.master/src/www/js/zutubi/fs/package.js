// dependency: ext/package.js
// dependency: ./FSTreeLoader.js
// dependency: ./LocalFileSystemBrowser.js
// dependency: ./PulseFileSystemBrowser.js
// dependency: ./WorkingCopyFileSystemBrowser.js

Zutubi.fs.viewWorkingCopy = function (project)
{
    var browser = new Zutubi.fs.WorkingCopyFileSystemBrowser({
        baseUrl : window.baseUrl,
        basePath: 'projects/' + project + '/latest/wc',
        title : 'browse working copy'
    });
    browser.show(this);
};
