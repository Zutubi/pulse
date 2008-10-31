form.items.last().on('browse', function(field)
{
    openFileDialog('/popups/pluginFileDialog.action', 'plugin.local', 'pluginPath', 'local:///', '', true, true, true);    
});