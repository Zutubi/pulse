(function(form, field)
{
    var ports = {'EMBEDDED': '0', 'MYSQL': '3306', 'POSTGRESQL': '5432'};

    function applyDefaults()
    {
        var port = ports[field.getValue()];
        if (port)
        {
            form.getFieldNamed('port').bindValue(port);
        }
    }

    form.bind('created', function(e)
    {
        applyDefaults();
    });

    field.bind('change', function(e)
    {
        applyDefaults();
    });
});
