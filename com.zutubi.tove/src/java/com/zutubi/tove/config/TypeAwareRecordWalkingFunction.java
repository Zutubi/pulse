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

package com.zutubi.tove.config;

import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.GraphFunction;

import java.util.Stack;

/**
 * Base class for record walking functions that need to be aware of the
 * types of the records.
 */
abstract class TypeAwareRecordWalkingFunction implements GraphFunction<Record>
{
    protected Stack<ComplexType> typeStack = new Stack<ComplexType>();
    protected Stack<Record> recordStack = new Stack<Record>();
    protected String path;

    public TypeAwareRecordWalkingFunction(ComplexType type, Record record, String path)
    {
        typeStack.push(type);
        recordStack.push(record);
        this.path = path;
    }

    public boolean push(String edge)
    {
        path = PathUtils.getPath(path, edge);
        Record record = (Record) recordStack.peek().get(edge);
        ComplexType type = (ComplexType) typeStack.peek().getActualPropertyType(edge, record);
        if (type != null)
        {
            typeStack.push(type);
            recordStack.push(record);
            return true;
        }
        else
        {
            return false;
        }
    }

    public void process(Record record)
    {
        process(path, record, typeStack.peek());
    }

    public void pop()
    {
        path = PathUtils.getParentPath(path);
        recordStack.pop();
        typeStack.pop();
    }

    protected abstract void process(String path, Record record, Type type);
}
