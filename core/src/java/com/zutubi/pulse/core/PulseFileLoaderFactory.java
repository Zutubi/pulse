package com.zutubi.pulse.core;

import java.util.Map;
import java.util.HashMap;

import com.zutubi.pulse.core.model.Property;

/**
 * A factory for creating PulseFileLoader objects that are aware of the
 * current plugins.
 */
public class PulseFileLoaderFactory
{
    private Map<String, Class> types = new HashMap<String, Class>();
    private ObjectFactory objectFactory;

    public PulseFileLoaderFactory()
    {
        register("property", Property.class);
        register("recipe", Recipe.class);
        register("def", ComponentDefinition.class);
        register("post-processor", PostProcessorGroup.class);
        register("command", CommandGroup.class);
        register("cppunit.pp", CppUnitReportPostProcessor.class);
        register("junit.pp", JUnitReportPostProcessor.class);
        register("junit.summary.pp", JUnitSummaryPostProcessor.class);
        register("ocunit.pp", OCUnitReportPostProcessor.class);
        register("regex-test.pp", RegexTestPostProcessor.class);
        register("regex.pp", RegexPostProcessor.class);
        register("executable", ExecutableCommand.class);
        register("print", PrintCommand.class);
        register("sleep", SleepCommand.class);
        register("resource", ResourceReference.class);
    }

    public PulseFileLoader createLoader()
    {
        PulseFileLoader loader = new PulseFileLoader();
        loader.setObjectFactory(objectFactory);
        for(Map.Entry<String, Class> entry: types.entrySet())
        {
            loader.register(entry.getKey(), entry.getValue());
        }

        return loader;
    }

    public void register(String name, Class type)
    {
        types.put(name, type);
    }

    public void unregister(String name)
    {
        types.remove(name);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
