#! /usr/bin/env bash

set -e

# Reports a fatal error and bails out
fatal()
{
    echo $1
    exit 1
}

if [[ $# -ne 1 ]]
then
    fatal "Usage: $0 <package>"
fi

package="$1"

if [[ ! -f "$package" ]]
then
    fatal "Package '$package' does not exist."
fi

# Prepare a temp directory to test within
top="$(pwd)"
tmpDir=pulse-accept
rm -rf $tmpDir
mkdir $tmpDir

# Unpack that shiny new package
extension="${package##*.}"
if [[ $extension == "gz" ]]
then
    tar -zxv -C $tmpDir -f "$package"
    extension=tar.gz
else
    unzip -d $tmpDir "$package"
fi

packageName="$(basename $package .$extension)"
pushd "$tmpDir/$packageName"

# Now create an appropriate database.config.properties file
case "$PULSE_DB" in
    postgresql)
        PULSE_DB_DRIVER=org.postgresql.Driver
        PULSE_DB_URL=jdbc:postgresql://localhost:5432/pulse_accept
        PULSE_DB_USER=pulsetest
        PULSE_DB_PASSWORD=pulsetest
        PULSE_DB_DIALECT=org.hibernate.dialect.PostgreSQLDialect

        if echo 'SELECT * FROM pg_database;' | PGPASSWORD=pulsetest psql -h localhost -U pulsetest -d template1 |grep pulse_accept
        then
            echo 'DROP DATABASE pulse_accept;' | PGPASSWORD=pulsetest psql -h localhost -U pulsetest -d template1
        fi
        echo 'CREATE DATABASE pulse_accept;' | PGPASSWORD=pulsetest psql -h localhost -U pulsetest -d template1
        ;;
    mysql)
        PULSE_DB_DRIVER=com.mysql.jdbc.Driver
        PULSE_DB_URL=jdbc:mysql://localhost:3306/pulse_accept
        PULSE_DB_USER=pulsetest
        PULSE_DB_PASSWORD=pulsetest
        PULSE_DB_DIALECT=org.hibernate.dialect.MySQLDialect

        echo 'DROP SCHEMA IF EXISTS pulse_accept; CREATE SCHEMA pulse_accept;' | mysql --user=pulsetest --password=pulsetest
        ;;
    *)
        PULSE_DB_DRIVER=org.hsqldb.jdbcDriver
        PULSE_DB_URL=jdbc:hsqldb:DB_ROOT/db
        PULSE_DB_USER=sa
        PULSE_DB_PASSWORD=
        PULSE_DB_DIALECT=org.hibernate.dialect.HSQLDialect
        ;;
esac

cat > versions/*/system/config/database.properties.template << EOF
jdbc.driverClassName=$PULSE_DB_DRIVER
jdbc.url=$PULSE_DB_URL
jdbc.username=$PULSE_DB_USER
jdbc.password=$PULSE_DB_PASSWORD
hibernate.dialect=$PULSE_DB_DIALECT
hibernate.show_sql=false
hibernate.jdbc.batch_size=0
hibernate.jdbc.use_scrollable_resultsets=false
EOF

cp "$top/master/src/acceptance/drivers/"* versions/*/lib

# Make sure selenium is running
if ! netstat -an | grep 4444 > /dev/null
then
    java -jar "../..//master/src/acceptance/misc/"selenium-server-*.jar > ../selenium-stdout.txt 2> ../selenium-stderr.txt &
    echo $! > ../selenium-pid.txt
fi

# Fire it up!
export PULSE_HOME=
"$(pwd)/bin/pulse" start -p 8889 -f config.properties > ../stdout.txt 2> ../stderr.txt

trap ERR

exit 0
