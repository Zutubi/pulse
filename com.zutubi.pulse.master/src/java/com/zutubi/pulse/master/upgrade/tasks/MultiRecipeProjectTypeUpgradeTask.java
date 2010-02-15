package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Pair;
import com.zutubi.util.StringUtils;
import com.zutubi.util.UnaryFunction;

import java.util.*;

import static com.zutubi.util.CollectionUtils.asPair;

/**
 * A task to upgrade the template-based builtin project types (Ant, Make, etc)
 * in 2.0 to single-command multi-recipe builds in 2.1.
 */
public class MultiRecipeProjectTypeUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PATH_PATTERN_PROJECT_TYPES = "projects/*/type";

    private static final String COMMAND_NAME = "build";
    private static final String RECIPE_NAME = "default";

    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_FORCE = "force";
    private static final String PROPERTY_DEFAULT_RECIPE = "defaultRecipe";
    private static final String PROPERTY_RECIPES = "recipes";
    private static final String PROPERTY_COMMANDS = "commands";
    private static final String PROPERTY_ARTIFACTS = "artifacts";
    private static final String PROPERTY_FAIL_IF_NOT_PRESENT = "failIfNotPresent";
    private static final String PROPERTY_IGNORE_STALE = "ignoreStale";
    private static final String PROPERTY_MIME_TYPE = "mimeType";
    private static final String PROPERTY_TYPE = "type";
    private static final String PROPERTY_INDEX = "index";
    private static final String PROPERTY_FOLLOW_SYMLINKS = "followSymlinks";
    private static final String PROPERTY_INCLUSIONS = "inclusions";
    private static final String PROPERTY_EXCLUSIONS = "exclusions";
    private static final String PROPERTY_BASE = "base";
    private static final String PROPERTY_INCLUDES = "includes";
    private static final String PROPERTY_EXCLUDES = "excludes";
    private static final String PROPERTY_FILE = "file";
    private static final String PROPERTY_OLD_POST_PROCESSORS = "postprocessors";
    private static final String PROPERTY_NEW_POST_PROCESSORS = "postProcessors";

    private static final String SYMBOLIC_NAME_RECIPE = "zutubi.recipeConfig";
    private static final String SYMBOLIC_NAME_MULTI_RECIPE_TYPE = "zutubi.multiRecipeTypeConfig";
    private static final String SYMBOLIC_NAME_FILE_ARTIFACT = "zutubi.fileArtifactConfig";
    private static final String SYMBOLIC_NAME_DIRECTORY_ARTIFACT = "zutubi.directoryArtifactConfig";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        TemplatedScopeDetails scopeDetails = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        Map<String, TypeMapping> typeMappings = buildTypeMappings();
        Map<String, String> ppMappings = buildPostProcessorMappings();

        RecordLocator recordLocator = RecordLocators.newPathPattern(PATH_PATTERN_PROJECT_TYPES);
        Map<String, Record> typeRecords = recordLocator.locate(recordManager);
        for (Map.Entry<String, Record> typeEntry : typeRecords.entrySet())
        {
            String symbolicName = typeEntry.getValue().getSymbolicName();
            TypeMapping mapping = typeMappings.get(symbolicName);
            if (mapping != null)
            {
                mapRecord(typeEntry.getKey(), typeEntry.getValue(), mapping, ppMappings, scopeDetails);
            }
        }
    }

    private HashMap<String, TypeMapping> buildTypeMappings()
    {
        HashMap<String, TypeMapping> mappings = new HashMap<String, TypeMapping>();

        mappings.put("zutubi.antTypeConfig", new TypeMapping("zutubi.antCommandConfig",
                asPair("work", "workingDir"),
                asPair("file", "buildFile"),
                asPair("target", "targets"),
                asPair("args", "args")));

        mappings.put("zutubi.bjamTypeConfig", new TypeMapping("zutubi.bjamCommandConfig",
                asPair("work", "workingDir"),
                asPair("file", "jamfile"),
                asPair("target", "targets"),
                asPair("args", "args")));

        mappings.put("zutubi.executableTypeConfig", new TypeMapping("zutubi.executableCommandConfig",
                asPair("workingDir", "workingDir"),
                asPair("executable", "exe"),
                asPair("arguments", "args")));

        mappings.put("zutubi.makeTypeConfig", new TypeMapping("zutubi.makeCommandConfig",
                asPair("workingDir", "workingDir"),
                asPair("makefile", "makefile"),
                asPair("targets", "targets"),
                asPair("arguments", "args")));

        mappings.put("zutubi.mavenTypeConfig", new TypeMapping("zutubi.mavenCommandConfig",
                asPair("workingDir", "workingDir"),
                asPair("targets", "targets"),
                asPair("arguments", "args")));

        mappings.put("zutubi.maven2TypeConfig", new TypeMapping("zutubi.maven2CommandConfig",
                asPair("workingDir", "workingDir"),
                asPair("goals", "goals"),
                asPair("arguments", "args")));

        TypeMapping msBuildMapping = new TypeMapping("zutubi.msbuildCommandConfig",
                asPair("workingDirectory", "workingDir"),
                asPair("buildFile", "buildFile"),
                asPair("targets", "targets"),
                asPair("configuration", "configuration"),
                asPair("arguments", "args"));
        msBuildMapping.addDeepCopiedProperty("buildProperties");
        mappings.put("zutubi.msbuildTypeConfig", msBuildMapping);

        TypeMapping xcodeMapping = new TypeMapping("zutubi.xcodeCommandConfig",
                asPair("workingDir", "workingDir"),
                asPair("config", "config"),
                asPair("project", "project"),
                asPair("target", "target"),
                asPair("action", "buildaction"),
                asPair("settings", "settings"));
        xcodeMapping.addPropertyFunction("settings", new UnaryFunction<Object, Object>()
        {
            public Object process(Object o)
            {
                return splitString((String) o);
            }
        });
        
        mappings.put("zutubi.xcodeTypeConfig", xcodeMapping);

        return mappings;
    }

    private Map<String, String> buildPostProcessorMappings()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("ant", "ant output processor");
        map.put("bjam", "boost jam output processor");
        map.put("boost-regression", "boost regression xml report processor");
        map.put("cppunit", "cppunit xml report processor");
        map.put("cunit", "cunit xml report processor");
        map.put("gcc", "gcc output processor");
        map.put("junit", "junit xml report processor");
        map.put("junitee","junitee xml report processor");
        map.put("junit-summary", "junit summary output processor");
        map.put("make", "make output processor");
        map.put("maven", "maven 1 output processor");
        map.put("maven2", "maven 2 output processor");
        map.put("msbuild", "msbuild output processor");
        map.put("ocunit", "ocunit output processor");
        map.put("unittestpp", "unittest++ xml report processor");
        map.put("xcode", "xcodebuild output processor");
        return map;
    }

    private void mapRecord(String path, Record oldTypeRecord, TypeMapping typeMapping, Map<String, String> postProcessorMappings, TemplatedScopeDetails scopeDetails)
    {
        boolean hasAncestor = scopeDetails.hasAncestor(path);
        ScopeHierarchy projectHierarchy = scopeDetails.getHierarchy();

        MutableRecord newCommandRecord = new MutableRecordImpl();
        newCommandRecord.setSymbolicName(typeMapping.toSymbolicName);
        if (!hasAncestor)
        {
            newCommandRecord.put(PROPERTY_NAME, COMMAND_NAME);
            newCommandRecord.put(PROPERTY_FORCE, "false");
        }

        typeMapping.mapProperties(oldTypeRecord, newCommandRecord);
        mapPostProcessors(oldTypeRecord, newCommandRecord, postProcessorMappings, projectHierarchy);
        mapArtifacts(oldTypeRecord, newCommandRecord, postProcessorMappings, projectHierarchy);

        MutableRecord newCommandsRecord = new MutableRecordImpl();
        newCommandsRecord.put(COMMAND_NAME, newCommandRecord);

        MutableRecord newRecipeRecord = new MutableRecordImpl();
        newRecipeRecord.setSymbolicName(SYMBOLIC_NAME_RECIPE);
        if (!hasAncestor)
        {
            newRecipeRecord.put(PROPERTY_NAME, RECIPE_NAME);
        }
        newRecipeRecord.put(PROPERTY_COMMANDS, newCommandsRecord);
        
        MutableRecord newRecipesRecord = new MutableRecordImpl();
        newRecipesRecord.put(RECIPE_NAME, newRecipeRecord);

        MutableRecord newTypeRecord = new MutableRecordImpl();
        newTypeRecord.setSymbolicName(SYMBOLIC_NAME_MULTI_RECIPE_TYPE);
        if (!hasAncestor)
        {
            newTypeRecord.put(PROPERTY_DEFAULT_RECIPE, RECIPE_NAME);
        }
        newTypeRecord.put(PROPERTY_RECIPES, newRecipesRecord);

        recordManager.delete(path);
        recordManager.insert(path, newTypeRecord);
    }

    private void mapArtifacts(Record oldTypeRecord, MutableRecord newCommandRecord, Map<String, String> postProcessorMappings, ScopeHierarchy projectHierarchy)
    {
        Record oldArtifacts = (Record) oldTypeRecord.get(PROPERTY_ARTIFACTS);
        MutableRecord newArtifacts = new MutableRecordImpl();
        for (String key: oldArtifacts.nestedKeySet())
        {
            Record oldArtifact = (Record) oldArtifacts.get(key);

            MutableRecord newArtifact = new MutableRecordImpl();
            String name = (String) copyValueIfPresent(oldArtifact, PROPERTY_NAME, newArtifact);
            boolean isRoot = name != null;
            if (isRoot)
            {
                newArtifact.put(PROPERTY_FAIL_IF_NOT_PRESENT, "true");
                newArtifact.put(PROPERTY_IGNORE_STALE, "false");
            }
            copyValueIfPresent(oldArtifact, PROPERTY_MIME_TYPE, newArtifact, PROPERTY_TYPE, null);

            if (oldArtifact.getSymbolicName().equals(SYMBOLIC_NAME_DIRECTORY_ARTIFACT))
            {
                newArtifact.setSymbolicName(SYMBOLIC_NAME_DIRECTORY_ARTIFACT);
                if (isRoot)
                {
                    newArtifact.put(PROPERTY_INDEX, "");
                    newArtifact.put(PROPERTY_FOLLOW_SYMLINKS, "false");
                }
                copyValueIfPresent(oldArtifact, PROPERTY_BASE, newArtifact);
                if (oldArtifact.containsKey(PROPERTY_INCLUDES))
                {
                    newArtifact.put(PROPERTY_INCLUSIONS, splitString((String) oldArtifact.get(PROPERTY_INCLUDES)));
                }
                if (oldArtifact.containsKey(PROPERTY_EXCLUDES))
                {
                    newArtifact.put(PROPERTY_EXCLUSIONS, splitString((String) oldArtifact.get(PROPERTY_EXCLUDES)));
                }
                mapPostProcessors(oldArtifact, newArtifact, postProcessorMappings, projectHierarchy);
            }
            else
            {
                newArtifact.setSymbolicName(SYMBOLIC_NAME_FILE_ARTIFACT);
                copyValueIfPresent(oldArtifact, PROPERTY_FILE, newArtifact);
            }

            newArtifacts.put(key, newArtifact);
        }
        
        newCommandRecord.put(PROPERTY_ARTIFACTS, newArtifacts);
    }

    private void mapPostProcessors(Record oldRecord, MutableRecord newRecord, Map<String, String> postProcessorMappings, ScopeHierarchy projectHierarchy)
    {
        String[] oldPostProcessors = (String[]) oldRecord.get(PROPERTY_OLD_POST_PROCESSORS);
        if (oldPostProcessors != null)
        {
            List<String> newPostProcessors = new LinkedList<String>();
            for (String oldName: oldPostProcessors)
            {
                String newProcessor = postProcessorMappings.get(oldName);
                if (newProcessor != null)
                {
                    long handle = getHandleForProcessor(newProcessor, projectHierarchy);
                    newPostProcessors.add(Long.toString(handle));
                }
            }

            newRecord.put(PROPERTY_NEW_POST_PROCESSORS, newPostProcessors.toArray(new String[newPostProcessors.size()]));
        }
    }

    private String[] splitString(String patterns)
    {
        if(StringUtils.stringSet(patterns))
        {
            List<String> result = StringUtils.split(patterns);
            return result.toArray(new String[result.size()]);
        }
        else
        {
            return new String[0];
        }
    }

    private long getHandleForProcessor(String displayName, ScopeHierarchy projectHierarchy)
    {
        String path = getPostProcessorPath(projectHierarchy.getRoot().getId(), displayName);
        Record ppRecord = recordManager.select(path);
        return ppRecord.getHandle();
    }

    private String getPostProcessorPath(String projectName, String processorName)
    {
        return SCOPE_PROJECTS + "/" + projectName + "/" + PROPERTY_NEW_POST_PROCESSORS + "/" + processorName;
    }

    private Object copyValueIfPresent(Record from, String key, MutableRecord to)
    {
        return copyValueIfPresent(from, key, to, key, null);
    }

    private Object copyValueIfPresent(Record from, String fromKey, MutableRecord to, String toKey, UnaryFunction<Object, Object> f)
    {
        Object value = from.get(fromKey);
        if (value != null)
        {
            if (f != null)
            {
                value = f.process(value);
            }

            to.put(toKey, value);
        }

        return value;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    private class TypeMapping
    {
        private String toSymbolicName;
        private Map<String, String> propertyMappings;
        private Map<String, UnaryFunction<Object, Object>> propertyFunctions = new HashMap<String, UnaryFunction<Object, Object>>();
        private Set<String> deepCopiedProperties = new HashSet<String>();

        public TypeMapping(String toSymbolicName, Pair<String, String>... propertyMappings)
        {
            this.toSymbolicName = toSymbolicName;
            this.propertyMappings = CollectionUtils.asMap(propertyMappings);
        }

        public void addPropertyFunction(String name, UnaryFunction<Object, Object> f)
        {
            propertyFunctions.put(name, f);
        }

        public void addDeepCopiedProperty(String name)
        {
            deepCopiedProperties.add(name);
        }

        public void mapProperties(Record oldTypeRecord, MutableRecord newCommandRecord)
        {
            for (Map.Entry<String, String> propertyMapping : propertyMappings.entrySet())
            {
                copyValueIfPresent(oldTypeRecord, propertyMapping.getKey(), newCommandRecord, propertyMapping.getValue(), propertyFunctions.get(propertyMapping.getKey()));
            }

            for (String property: deepCopiedProperties)
            {
                Record value = (Record) oldTypeRecord.get(property);
                if (value != null)
                {
                    newCommandRecord.put(property, value.copy(true, true));
                }
            }
        }
    }
}
