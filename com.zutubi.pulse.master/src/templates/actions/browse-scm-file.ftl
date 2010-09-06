${form.name}.items.last().on('browse', function(field)
{
    function findProjectPath(path)
    {
<#if field.parameters.baseName?exists>
        var start = 'c';
<#else>
        var start = 'wizards/';
</#if>
        var parentPath = getParentPath(path);
        while(parentPath != null && parentPath != 'projects')
        {
            path = parentPath;
            parentPath = getParentPath(path);
            start = 'c';
        }

        return start + path;
    }

    var projectPath = findProjectPath('${field.parameters.path?js_string}');
    var prefix = '';
<#if field.parameters.baseDirField?exists>
    var dirField = Ext.getCmp('zfid.${field.parameters.baseDirField}');
    prefix += dirField.getValue();
</#if>
<#assign title = "${field.name}" + ".popup.title"/>
    var browser = new ZUTUBI.WorkingCopyFileSystemBrowser({
        baseUrl : '${base}',
        showFiles: true,
        prefix:prefix,
        basePath: projectPath + '/scm/' + prefix,
        title : '${title?i18n}',
        target : '${parameters.id?js_string}',
        onClose: function()
        {
            updateButtons();
        }
    });
    browser.show();
});
