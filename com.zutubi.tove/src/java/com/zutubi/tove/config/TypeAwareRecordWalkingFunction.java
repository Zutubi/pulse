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
