package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.adt.Pair;

import java.util.Map;

import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.*;

/**
 * Abstract base for upgrades that add patterns to existing regex processors.
 */
public abstract class AbstractRegexProcessorPatternsUpgradeTask extends AbstractUpgradeTask
{
    protected static final String CATEGORY_ERROR = "ERROR";
    protected static final String CATEGORY_INFO = "INFO";
    protected static final String CATEGORY_WARNING = "WARNING";

    private static final String SCOPE_PROJECTS = "projects";

    private static final String PATH_PATTERN_ALL_PROCESSORS = "projects/*/postProcessors/*";

    private static final String TYPE_REGEX_PATTERN = "zutubi.regexPatternConfig";

    private static final String PROPERTY_PATTERNS = "patterns";
    private static final String PROPERTY_EXPRESSION = "expression";
    private static final String PROPERTY_CATEGORY = "category";
    private static final String PROPERTY_EXCLUSIONS = "exclusions";
    
    private RecordManager recordManager;
    private TemplatedScopeDetails scope;
    private TransactionManager transactionManager;

    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute() throws TaskException
    {
        PersistentScopes scopes = new PersistentScopes(recordManager);
        scope = (TemplatedScopeDetails) scopes.getScopeDetails(SCOPE_PROJECTS);

        transactionManager.runInTransaction(new NullaryFunction()
        {
            public Object process()
            {
                RecordLocator locator = newFirstDefinedFilter(newTypeFilter(newPathPattern(PATH_PATTERN_ALL_PROCESSORS), getProcessorType()), scope);
                Map<String, Record> processors = locator.locate(recordManager);
                for (Map.Entry<String, Record> entry: processors.entrySet())
                {
                    addPatternsIfNotPresent(entry.getKey(), entry.getValue());
                }
                
                return null;
            }
        });
    }

    private void addPatternsIfNotPresent(String path, Record processorRecord)
    {
        Record patternsRecord = (Record) processorRecord.get(PROPERTY_PATTERNS);
        if (patternsRecord != null)
        {
            for (Pair<String, String> pattern: getPatterns())
            {
                addPatternIfNotPresent(path, patternsRecord, pattern.first, pattern.second);
            }
        }
    }

    private void addPatternIfNotPresent(String path, Record patternsRecord, String category, String pattern)
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
        patternRecord.put(PROPERTY_CATEGORY, category);
        patternRecord.put(PROPERTY_EXPRESSION, pattern);
        patternRecord.put(PROPERTY_EXCLUSIONS, new String[0]);

        RecordUpgradeUtils.insertWithSkeletons(PathUtils.getPath(path, PROPERTY_PATTERNS, Long.toString(recordManager.allocateHandle())), patternRecord, scope, recordManager);
    }

    /**
     * Indicates the type of processor this upgrade applies to.
     * 
     * @return the symbolic name of the processor to apply this upgrade to
     */
    protected abstract String getProcessorType();

    /**
     * Returns a set of (category, expression) pairs for the patterns to be
     * added.
     * 
     * @return a set of (category, expression) pairs to be added to processors
     */
    protected abstract Iterable<? extends Pair<String, String>> getPatterns();

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setPulseTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }
}