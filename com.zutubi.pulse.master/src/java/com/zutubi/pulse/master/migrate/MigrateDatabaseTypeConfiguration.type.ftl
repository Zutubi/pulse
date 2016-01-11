(function(form, field)
{
    var ports = {'EMBEDDED': '0', 'MYSQL': '3306', 'POSTGRESQL': '5432'},
        drivers = {'EMBEDDED': 'org.hsqldb.jdbcDriver', 'MYSQL': 'com.mysql.jdbc.Driver', 'POSTGRESQL': 'org.postgresql.Driver'};

    function applyDefaults()
    {
        var db = field.getValue(), port, driver;
        port = ports[db];
        if (port)
        {
            form.getFieldNamed('port').bindValue(port);
        }

        driver = drivers[db];
        if (driver)
        {
            form.getFieldNamed('driver').bindValue(driver);
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
