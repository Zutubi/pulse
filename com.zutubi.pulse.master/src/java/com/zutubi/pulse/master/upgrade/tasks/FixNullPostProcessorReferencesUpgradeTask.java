package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.Record;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.UnaryFunction;

import java.util.List;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;

/**
 * Fixes null post-processor references that may have been created by a bug in
 * smart clone.  See CIB-2562.
 */
public class FixNullPostProcessorReferencesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_TYPE = "type";
    private static final String PROPERTY_RECIPES = "recipes";
    private static final String PROPERTY_COMMANDS = "commands";
    private static final String PROPERTY_ARTIFACTS = "artifacts";
    private static final String PROPERTY_POST_PROCESSORS = "postProcessors";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator locator = RecordLocators.newUnion(
                RecordLocators.newPathPattern(getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_TYPE, PROPERTY_RECIPES, WILDCARD_ANY_ELEMENT, PROPERTY_COMMANDS, WILDCARD_ANY_ELEMENT)),
                RecordLocators.newPathPattern(getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_TYPE, PROPERTY_RECIPES, WILDCARD_ANY_ELEMENT, PROPERTY_COMMANDS, WILDCARD_ANY_ELEMENT, PROPERTY_ARTIFACTS, WILDCARD_ANY_ELEMENT))
        );
        
        return new PredicateFilterRecordLocator(locator, new Predicate<Record>()
        {
            public boolean satisfied(Record record)
            {
                return record.containsKey(PROPERTY_POST_PROCESSORS);
            }
        });
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return asList(RecordUpgraders.newEditProperty(PROPERTY_POST_PROCESSORS, new UnaryFunction<Object, Object>()
        {
            public Object process(Object o)
            {
                if (o != null && o instanceof String[])
                {
                    String[] array = (String[]) o;
                    return CollectionUtils.filterToArray(array, new Predicate<String>()
                    {
                        public boolean satisfied(String s)
                        {
                            return !s.equals("0");
                        }
                    });
                }
                
                return o;
            }
        }));
    }
}