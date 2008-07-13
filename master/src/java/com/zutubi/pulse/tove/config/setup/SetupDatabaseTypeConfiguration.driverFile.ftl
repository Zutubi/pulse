form.items.last().on('browse', function(field)
{
    openFileDialog('${base}/popups/fileDialog.action', '${form.name}', field.name, 'local:///', '', true, false, true);
});
