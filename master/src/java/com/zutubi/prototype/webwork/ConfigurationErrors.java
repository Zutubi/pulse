package com.zutubi.prototype.webwork;

import com.opensymphony.xwork.ValidationAware;
import com.zutubi.util.StringUtils;
import flexjson.JSON;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that holds errors in a form that serialises to JSON in a way that is
 * understood by the client side.
 */
public class ConfigurationErrors
{
    private ValidationAware aware;

    public ConfigurationErrors(ValidationAware aware)
    {
        this.aware = aware;
    }

    public boolean getSuccess()
    {
        return false;
    }

    @JSON
    @SuppressWarnings({"unchecked"})
    public FieldError[] getErrors()
    {
        Set<Map.Entry<String,List<String>>> entries = aware.getFieldErrors().entrySet();
        FieldError[] result = new FieldError[entries.size()];
        int i = 0;
        for(Map.Entry<String, List<String>> entry: entries)
        {
            result[i++] = new FieldError(entry.getKey(), StringUtils.join("<br/>", entry.getValue()));
        }

        return result;
    }

    public static class FieldError
    {
        private String id;
        private String msg;

        public FieldError(String id, String msg)
        {
            this.id = id;
            this.msg = msg;
        }

        public String getId()
        {
            return id;
        }

        public String getMsg()
        {
            return msg;
        }
    }
}
