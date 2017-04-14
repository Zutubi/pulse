/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.transfer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class TransferTable implements Table
{
    private String name;
    private List<Column> columns;

    private Map<String, Integer> columnTypes;
    private Map<String, Column> columnsByName;

    public TransferTable()
    {

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Column> getColumns()
    {
        if (columns == null)
        {
            columns = new LinkedList<Column>();
        }
        return columns;
    }

    private Map<String, Integer> getColumnTypes()
    {
        if (columnTypes == null)
        {
            columnTypes = new HashMap<String, Integer>();
        }
        return columnTypes;
    }

    private Map<String, Column> getColumnsByName()
    {
        if (columnsByName == null)
        {
            columnsByName = new HashMap<String, Column>();
        }
        return columnsByName;
    }

    public void add(Column column)
    {
        getColumns().add(column);
        getColumnTypes().put(column.getName(), column.getSqlTypeCode());
        getColumnsByName().put(column.getName(), column);
    }

    public Integer getColumnType(String name)
    {
        return getColumnTypes().get(name);
    }

    public Column getColumn(String name)
    {
        return getColumnsByName().get(name);
    }

    public void remove(Column column)
    {
        this.columns.remove(column);
        this.columnsByName.remove(column.getName());
        this.columnTypes.remove(column.getName());
    }
    
    public TransferTable copy()
    {
        TransferTable copy = new TransferTable();
        copy.name = this.name;
        copy.columns = new LinkedList<Column>(this.columns);
        copy.columnsByName = new HashMap<String, Column>(this.columnsByName);
        copy.columnTypes = new HashMap<String, Integer>(this.columnTypes);
        
        return copy;
    }
}
