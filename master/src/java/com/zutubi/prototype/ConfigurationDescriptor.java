package com.zutubi.prototype;

import com.zutubi.pulse.prototype.record.RecordTypeInfo;
import com.zutubi.pulse.prototype.record.SimpleRecordPropertyInfo;
import com.zutubi.pulse.prototype.record.ValueListRecordPropertyInfo;
import com.zutubi.pulse.prototype.record.SubrecordRecordPropertyInfo;
import com.zutubi.prototype.model.Config;

import java.util.List;

/**
 *
 *
 */
public class ConfigurationDescriptor
{
    private RecordTypeInfo typeInfo;

    public void setTypeInfo(RecordTypeInfo typeInfo)
    {
        this.typeInfo = typeInfo;
    }

    public Config instantiate(Object value)
    {
        Config config = new Config();
        List<SimpleRecordPropertyInfo> simpleInfos = typeInfo.getSimpleInfos();
        if (simpleInfos != null)
        {
            for (SimpleRecordPropertyInfo info : simpleInfos)
            {
                config.addSimpleProperty(info.getName());
            }
        }

        List<ValueListRecordPropertyInfo> valueListInfos = typeInfo.getValueListInfos();
        if (valueListInfos != null)
        {
            for (ValueListRecordPropertyInfo info : valueListInfos)
            {
                config.addValueListProperty(info.getName());
            }
        }

        List<SubrecordRecordPropertyInfo> subrecordInfos = typeInfo.getSubrecordInfos();
        if (subrecordInfos != null)
        {
            for (SubrecordRecordPropertyInfo info: subrecordInfos)
            {
                config.addNestedProperty(info.getName());
            }
        }
        return config;
    }

}
