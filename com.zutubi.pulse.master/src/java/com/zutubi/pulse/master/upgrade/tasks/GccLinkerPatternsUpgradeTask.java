package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.UnaryFunction;

import java.util.Map;

/**
 * Task to add new expressions to GCC processor to catch linker errors.
 */
public class GccLinkerPatternsUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";

    private static final String PATH_PATTERN_ALL_PROCESSORS = "projects/*/postProcessors/*";

    private static final String TYPE_GCC_PROCESSOR = "zutubi.gccPostProcessorConfig";
    private static final String TYPE_REGEX_PATTERN = "zutubi.regexPatternConfig";

    private static final String PROPERTY_PATTERNS = "patterns";
    private static final String PROPERTY_EXPRESSION = "expression";
    private static final String PROPERTY_CATEGORY = "category";
    private static final String PROEPRTY_EXCLUSIONS = "exclusions";

    private static final String PATTERN_UNDEFINED  = ": undefined reference to";
    private static final String PATTERN_ERROR_EXIT = "^collect2: ld returned [1-9][0-9]* exit status";

    private static final String CATEGORY_ERROR = "ERROR";
    
    private RecordManager recordManager;
    private TemplatedScopeDetails scope;

    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute() throws TaskException
    {
        PersistentScopes scopes = new PersistentScopes(recordManager);
        scope = (TemplatedScopeDetails) scopes.getScopeDetails(SCOPE_PROJECTS);

        Map<String, Record> processors = recordManager.selectAll(PATH_PATTERN_ALL_PROCESSORS);
        for (Map.Entry<String, Record> entry: processors.entrySet())
        {
            String path = entry.getKey();
            Record processorRecord = entry.getValue();
            if (TYPE_GCC_PROCESSOR.equals(processorRecord.getSymbolicName()) && !scope.hasAncestor(path))
            {
                addPatternsIfNotPresent(path, processorRecord);
            }
        }
    }

    private void addPatternsIfNotPresent(String path, Record processorRecord)
    {
        Record patternsRecord = (Record) processorRecord.get(PROPERTY_PATTERNS);
        if (patternsRecord != null)
        {
            addPatternIfNotPresent(path, patternsRecord, PATTERN_UNDEFINED);
            addPatternIfNotPresent(path, patternsRecord, PATTERN_ERROR_EXIT);
        }
    }

    private void addPatternIfNotPresent(String path, Record patternsRecord, String pattern)
    {
        for (String key: patternsRecord.nestedKeySet())
        {
            Record patternRecord = (Record) patternsRecord.get(key);
            if (pattern.equals(patternRecord.get(PROPERTY_EXPRESSION)))
            {
                return;
            }
        }

        MutableRecordImpl patternRecord = new MutableRecordImpl();
        patternRecord.setSymbolicName(TYPE_REGEX_PATTERN);
        patternRecord.put(PROPERTY_CATEGORY, CATEGORY_ERROR);
        patternRecord.put(PROPERTY_EXPRESSION, pattern);
        patternRecord.put(PROEPRTY_EXCLUSIONS, new String[0]);

        String insertPath = PathUtils.getPath(path, PROPERTY_PATTERNS, Long.toString(recordManager.allocateHandle()));
        recordManager.insert(insertPath, patternRecord);

        addSkeletons(insertPath);
    }

    private void addSkeletons(String path)
    {
        final String[] elements = PathUtils.getPathElements(path);
        final String remainderPath = PathUtils.getPath(2, elements);
        ScopeHierarchy.Node owner = scope.getHierarchy().findNodeById(elements[1]);

        final MutableRecordImpl skeleton = new MutableRecordImpl();
        skeleton.setSymbolicName(TYPE_REGEX_PATTERN);

        owner.forEach(new UnaryFunction<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean process(ScopeHierarchy.Node node)
            {
                String path = PathUtils.getPath(elements[0], node.getId(), remainderPath);
                if (!recordManager.containsRecord(path))
                {
                    recordManager.insert(path, skeleton);
                }

                return true;
            }
        });
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
