package com.zutubi.pulse.master.rest.model;

/**
 * Models a key-value property, with an option human-friendly label.  Supported value types are:
 * <ul>
 *     <li>String</li>
 *     <li>Collection&lt;String&gt;</li>
 *     <li>Collection&lt;KeyValueModel&gt;</li>
 * </ul>
 * In the latter case only one layer of nesting is allowed.
 */
public class KeyValueModel
{
    private String key;
    private String label;
    private Object value;

    public KeyValueModel(String key, Object value)
    {
        this.key = key;
        this.value = value;
    }

    public KeyValueModel(String key, String label, Object value)
    {
        this.key = key;
        this.label = label;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }

    public Object getValue()
    {
        return value;
    }
}
