package com.zutubi.prototype.type.record;

import com.zutubi.prototype.type.record.store.RecordStore;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 *
 *
 */
public class RecordQueries
{
    private Record base;

    public static RecordQueries getQueries(RecordStore store)
    {
        return new RecordQueries(store.select());
    }

    public RecordQueries(Record record)
    {
        this.base = record;
    }

    public List<String> selectPaths(String pattern)
    {
        List<String> paths = new LinkedList<String>();
        getAllPaths(base, PathUtils.getPathElements(pattern), 0, "", paths);
        return paths;
    }

    public Record select(String path)
    {
        String[] elements = PathUtils.getPathElements(path);

        Record record = base;
        for (String pathElement : elements)
        {
            Object data = record.get(pathElement);
            if (data == null || !(data instanceof Record))
            {
                return null;
            }
            record = (Record) data;
        }
        return record;
    }

    public Map<String, Record> selectAll(String pattern)
    {
        Map<String, Record> records = new HashMap<String, Record>();
        selectAll(base, PathUtils.getPathElements(pattern), 0, "", records);
        return records;
    }

    private void selectAll(Record record, String[] elements, int pathIndex, String resolvedPath, Map<String, Record> records)
    {
        if (pathIndex == elements.length)
        {
            records.put(resolvedPath, record);
            return;
        }

        for (String key : record.nestedKeySet())
        {
            if (PathUtils.elementMatches(elements[pathIndex], key))
            {
                selectAll((Record) record.get(key), elements, pathIndex + 1, PathUtils.getPath(resolvedPath, key), records);
            }
        }
    }

    private void getAllPaths(Record record, String[] elements, int pathIndex, String resolvedPath, List<String> paths)
    {
        if (pathIndex == elements.length)
        {
            paths.add(resolvedPath);
        }
        else
        {
            for (String key : record.nestedKeySet())
            {
                if (PathUtils.elementMatches(elements[pathIndex], key))
                {
                    getAllPaths((Record) record.get(key), elements, pathIndex + 1, PathUtils.getPath(resolvedPath, key), paths);
                }
            }
        }
    }
}
