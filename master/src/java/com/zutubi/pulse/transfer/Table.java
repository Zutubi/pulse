package com.zutubi.pulse.transfer;

import java.util.List;

/**
 *
 *
 */
public interface Table
{
    String getName();
    void setName(String name);

    List<Column> getColumns();

    Column getColumn(String name);
}

