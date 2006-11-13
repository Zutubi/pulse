package com.zutubi.pulse.upgrade;

import javax.sql.DataSource;

/**
 * Interface for upgrade tasks that require access to the data source to
 * perform the upgrade.
 */
public interface DataSourceAware
{
    public void setDataSource(DataSource source);
}
