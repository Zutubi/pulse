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

package com.zutubi.tove.ui.model.tables;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a table: a way to display a collection in the UI.
 */
public class TableModel
{
    private String heading;
    private List<ColumnModel> columns = new ArrayList<>();

    public TableModel(String heading)
    {
        this.heading = heading;
    }

    public String getHeading()
    {
        return heading;
    }

    public List<ColumnModel> getColumns()
    {
        return columns;
    }

    public void addColumn(ColumnModel column)
    {
        columns.add(column);
    }
}
