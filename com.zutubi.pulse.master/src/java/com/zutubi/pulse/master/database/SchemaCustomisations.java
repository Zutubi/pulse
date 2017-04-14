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

package com.zutubi.pulse.master.database;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hand-applied schema customisations - for things that hibernate does not support.
 */
public class SchemaCustomisations
{
    public static String[] customiseSchemaCreationScript(Connection connection, String[] generatedSql) throws SQLException
    {
        String[] result = new String[generatedSql.length + 1];
        System.arraycopy(generatedSql, 0, result, 0, generatedSql.length);
        result[generatedSql.length] = JDBCUtils.sqlAddIndex(connection, "AGENT_SYNCH_MESSAGE", "AGENT_SYNCH_MESSAGE_DESCRIPTION", "DESCRIPTION", 127);
        return result;
    }
}
