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

package com.zutubi.pulse.master.transfer.jdbc;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;

import java.sql.Types;

/**
 *
 *
 */
public class HibernateUniqueKeyTable
{
    public static boolean isTable(String tableName)
    {
        return tableName.equals("hibernate_unique_key");
    }

    public static Table getMapping()
    {
        Table table = new Table("hibernate_unique_key");
        Column column = new Column("next_hi");
        SimpleValue value = new SimpleValue(null, table);
        value.setTypeName(int.class.getName());
        column.setValue(value);
        column.setSqlTypeCode(Types.INTEGER);
        table.addColumn(column);
        return table;
    }
}
