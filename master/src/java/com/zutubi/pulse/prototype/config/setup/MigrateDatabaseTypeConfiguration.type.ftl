form.items.last().on('select', function(field)
{
    var ports = {'EMBEDDED': '0', 'MYSQL': '3306', 'POSTGRESQL': '5432'};
    var port = ports[field.getValue()];
    if(port)
    {
        form.findById('zfid.port').setValue(port);
    }

    var drivers = {'EMBEDDED': 'org.hsqldb.jdbcDriver', 'MYSQL': 'com.mysql.jdbc.Driver', 'POSTGRESQL': 'org.postgresql.Driver'};
    var driver = drivers[field.getValue()];
    if (driver)
    {
        form.findById('zfid.driver').setValue(driver);
    }
});
