${form.name}.items.last().on('browse', function(field)
{
    openFileDialog('${base}/popups/databaseFileDialog.action', '${form.name}', field.name, 'local:///', '', true, false, true);
});
