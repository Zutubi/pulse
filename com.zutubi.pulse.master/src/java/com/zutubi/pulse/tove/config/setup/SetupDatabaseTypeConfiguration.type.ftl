form.items.last().on('select', function(field)
{
    var ports = {'EMBEDDED': '0', 'MYSQL': '3306', 'POSTGRESQL': '5432'};
    var port = ports[field.getValue()];
    if(port)
    {
        form.findById('zfid.port').setValue(port);
    }
});
