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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adds indices to the new BUILD_DEPENDENCY_LINK table.
 */
public class DependencyLinkIndicesUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        addIndex(con, "BUILD_DEPENDENCY_LINK", "idx_builddependencylink_upstreambuildid", "UPSTREAM_BUILD_ID");
        addIndex(con, "BUILD_DEPENDENCY_LINK", "idx_builddependencylink_downstreambuildid", "DOWNSTREAM_BUILD_ID");
    }
}
