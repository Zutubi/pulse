package com.zutubi.tove.config.health;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.config.ConfigurationPersistenceManager;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationScopeInfo;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.zutubi.tove.type.record.PathUtils.getPath;
import static com.zutubi.tove.type.record.PathUtils.getPathElements;

/**
 * The health checker can be used to check the internal consistency of a tove
 * configuration store.  It can report things like bad/broken references,
 * illegal inheritance structures and so on.
 * <p/>
 * Results are return in a health report which is suitable for human
 * consumption.
 *
 * @see ConfigurationHealthReport
 */
public class ConfigurationHealthChecker
{
    private static final Messages I18N = Messages.getInstance(ConfigurationHealthChecker.class);

    /**
     * Maximum number of passes we will attempt to make to heal a path.
     */
    private static final int PASS_LIMIT = 6;

    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private RecordManager recordManager;
    private TypeRegistry typeRegistry;

    /**
     * Run all checks over the entire record store, reporting any problems
     * found.
     *
     * @return a report listing all problems found
     * 
     * @see #checkPath(String) 
     */
    public ConfigurationHealthReport checkAll()
    {
        ConfigurationHealthReport report = new ConfigurationHealthReport();
        checkRoot(report);
        return report;
    }

    /**
     * Runs checks over the given path, reporting any problems found.  If the
     * path is empty, the entire record store is checked.  The path must exist.
     * <p/>
     * Note that template parent pointers are only checked at the level of the
     * templated collection items - if you pass a path with more than two
     * elements the parent pointer is unchecked.
     * 
     * @param path path to check
     * @return a report listing all problems found
     * 
     * @see #checkAll() 
     */
    public ConfigurationHealthReport checkPath(String path)
    {        
        String[] elements = getPathElements(path);
        if (elements.length == 0)
        {
            return checkAll();
        }
        else
        {
            if (!configurationTemplateManager.pathExists(path))
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            ConfigurationScopeInfo scope = configurationPersistenceManager.getScopeInfo(elements[0]);
            ConfigurationHealthReport report = new ConfigurationHealthReport();

            if (elements.length == 1)
            {
                checkScope(scope, recordManager.select(scope.getScopeName()), report);
            }
            else if (scope.isTemplated())
            {
                if (elements.length == 2)
                {
                    CompositeType templatedItemType = (CompositeType) scope.getType().getTargetType();
                    Record templatedCollectionRecord = recordManager.select(scope.getScopeName());
                    String itemKey = elements[1];
                    if (checkTemplatedCollectionItemStructure(scope.getScopeName(), templatedItemType, templatedCollectionRecord, itemKey, report))
                    {
                        checkTemplatedCollectionItem(scope.getScopeName(), templatedItemType, itemKey, report);
                    }
                }
                else
                {
                    TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord(path);
                    TemplateRecord templateParentRecord = record.getParent();
                    if (templateParentRecord == null || checkStructuresMatch(path, record.getMoi(), getTemplateParentPath(path, templateParentRecord), templateParentRecord.getMoi(), report))
                    {
                        checkRecord(path, configurationTemplateManager.getType(path), record, report);
                    }
                }
            }
            else
            {
                checkRecord(path, configurationTemplateManager.getType(path), recordManager.select(path), report);
            }

            return report;
        }
    }
    
    /**
     * Finds and automatically heals all configuration problems, if possible.
     * This may involve a few passes over the configuration, as healing one
     * problem can allow others to be uncovered (or even cause others - e.g. a
     * value may be fixed and then need to be scrubbed).
     * <p/>
     * If a pass results in no changes, too many passes are required, or an
     * unsolvable problem is found, healing is stopped.
     * 
     * @return a health report giving the current state of the configuration
     *         when this method returns.  If the report is healthy, then no
     *         further problems exist.  Otherwise, there may be unsolvable
     *         problems, or the pass limit may have been reached.
     * 
     * @see #healPath(String) 
     */
    public ConfigurationHealthReport healAll()
    {
        return healPath("");
    }
    
    /**
     * Finds and automatically heals configuration problems under the given
     * path, if possible.  Refer to {@link #healAll()} for more details. 
     * 
     * @return a health report giving the current state of the configuration
     *         when this method returns
     * 
     * @see #healAll()  
     */
    public ConfigurationHealthReport healPath(String path)
    {
        ConfigurationHealthReport previousReport = null;
        ConfigurationHealthReport report = checkPath(path);
        int passes = 0;
        while (!report.isHealthy() && report.isSolvable() && passes++ < PASS_LIMIT && !report.equals(previousReport))
        {
            solve(report);
            previousReport = report;
            report = checkPath(path);
        }
        
        return report;
    }

    private void solve(ConfigurationHealthReport report)
    {
        for (HealthProblem problem: report.getProblems())
        {
            problem.solve(recordManager);
        }
    }
    
    private String getTemplateParentPath(String path, TemplateRecord templateParentRecord)
    {
        String[] elements = getPathElements(path);
        elements[1] = templateParentRecord.getOwner();
        return getPath(elements);
    }
    
    private void checkRoot(ConfigurationHealthReport report)
    {
        Record root = recordManager.select();
        for (String key : root.simpleKeySet())
        {
            report.addProblem(new UnexpectedSimpleValueProblem("", I18N.format("root.simple.key", key), key));
        }

        for (String key : root.nestedKeySet())
        {
            ConfigurationScopeInfo scope = configurationPersistenceManager.getScopeInfo(key);
            if (scope == null)
            {
                report.addProblem(new UnexpectedNestedRecordProblem("", I18N.format("root.unexpected.scope", key), key));
            }
            else
            {
                Record record = (Record) root.get(key);
                checkScope(scope, record, report);
            }
        }
    }

    private void checkScope(ConfigurationScopeInfo scope, Record record, ConfigurationHealthReport report)
    {
        ComplexType type = scope.getType();
        String scopeName = scope.getScopeName();
        if (checkType(scopeName, type, record, report) == null)
        {
            return;
        }

        if (scope.isTemplated())
        {
            CompositeType targetType = (CompositeType) type.getTargetType();
            if (checkTemplateHierarchy(scopeName, targetType, record, report))
            {
                checkTemplatedCollection(scopeName, targetType, record, report);
            }
        }
        else
        {
            checkRecord(scopeName, type, record, report);
        }
    }

    private boolean checkTemplateHierarchy(String scopeName, CompositeType type, Record templateCollectionRecord, ConfigurationHealthReport report)
    {
        int problemsBefore = report.getProblemCount();

        checkForSimpleKeys(scopeName, templateCollectionRecord, report);
        if (checkRootItem(scopeName, type, templateCollectionRecord, report))
        {
            for (String item : templateCollectionRecord.nestedKeySet())
            {
                checkTemplatedCollectionItemStructure(scopeName, type, templateCollectionRecord, item, report);
            }
        }
        
        return problemsBefore == report.getProblemCount();
    }

    private boolean checkRootItem(String scopeName, CompositeType type, Record templateCollectionRecord, ConfigurationHealthReport report)
    {
        String rootItem = null;
        for (String item : templateCollectionRecord.nestedKeySet())
        {
            String path = getPath(scopeName, item);
            Record record = (Record) templateCollectionRecord.get(item);
            if (checkType(path, type, record, report) != null)
            {
                String parentHandleString = record.getMeta(TemplateRecord.PARENT_KEY);
                if (parentHandleString == null)
                {
                    if (rootItem == null)
                    {
                        rootItem = item;
                    }
                    else
                    {
                        List<String> roots = Arrays.asList(rootItem, item);
                        Collections.sort(roots);
                        report.addProblem(new UnsolvableHealthProblem(scopeName, I18N.format("scope.multiple.roots", roots.get(0), roots.get(1))));
                        return false;
                    }

                    if (!configurationTemplateManager.isTemplate(record))
                    {
                        // We could mark it as a template, but we are in
                        // unknown territory so it seems safer not to try.
                        report.addProblem(new UnsolvableHealthProblem(path, I18N.format("scope.root.not.template", rootItem)));
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkTemplatedCollectionItemStructure(String scopeName, CompositeType type, Record templateCollectionRecord, String itemKey, ConfigurationHealthReport report)
    {
        int problemsBefore = report.getProblemCount();

        String path = getPath(scopeName, itemKey);
        Record record = (Record) templateCollectionRecord.get(itemKey);
        String parentHandleString = record.getMeta(TemplateRecord.PARENT_KEY);
        if (parentHandleString != null)
        {
            try
            {
                long templateParentHandle = Long.parseLong(parentHandleString);
                String templateParentPath = recordManager.getPathForHandle(templateParentHandle);
                if (templateParentPath == null)
                {
                    report.addProblem(new UnsolvableHealthProblem(path, I18N.format("parent.handle.unknown", templateParentHandle)));
                }
                else
                {
                    String[] templateParentPathElements = getPathElements(templateParentPath);
                    if (templateParentPathElements.length != 2 || !templateParentPathElements[0].equals(scopeName))
                    {
                        report.addProblem(new UnsolvableHealthProblem(path, I18N.format("parent.not.in.collection", templateParentPath)));
                    }
                    else
                    {
                        Record templateParentRecord = recordManager.select(templateParentPath);
                        if (templateParentRecord == null)
                        {
                            // This should not happen, since we looked up
                            // the handle using the record manager.  But
                            // the checker is a paranoid beast.
                            report.addProblem(new UnsolvableHealthProblem(path, I18N.format("parent.path.invalid", templateParentPath)));
                        }
                        else
                        {
                            if (!configurationTemplateManager.isTemplate(templateParentRecord))
                            {
                                report.addProblem(new UnsolvableHealthProblem(path, I18N.format("parent.not.template", templateParentPath)));
                            }
                            else
                            {
                                checkStructuresMatch(path, record, templateParentPath, templateParentRecord, report);
                            }
                        }
                    }
                }
            }
            catch (NumberFormatException e)
            {
                report.addProblem(new UnsolvableHealthProblem(path, I18N.format("parent.handle.illegal", parentHandleString)));
            }
        }

        return problemsBefore == report.getProblemCount();
    }

    private boolean checkStructuresMatch(String path, Record record, String templateParentPath, Record templateParentRecord, ConfigurationHealthReport report)
    {
        for (String key : templateParentRecord.nestedKeySet())
        {
            if (record.containsKey(key))
            {
                Object nested = record.get(key);
                if (!(nested instanceof Record))
                {
                    report.addProblem(new UnsolvableHealthProblem(path, I18N.format("inherited.record.simple.in.child", key)));
                    return false;
                }

                String childPath = getPath(path, key);
                Record nestedRecord = (Record) nested;
                Record nestedTemplateParentRecord = (Record) templateParentRecord.get(key);
                if (!StringUtils.equals(nestedRecord.getSymbolicName(), nestedTemplateParentRecord.getSymbolicName()))
                {
                    report.addProblem(new UnsolvableHealthProblem(childPath, I18N.format("inherited.type.mismatch", nestedRecord.getSymbolicName(), nestedTemplateParentRecord.getSymbolicName())));
                    return false;
                }

                if (!checkStructuresMatch(getPath(path, key), nestedRecord, getPath(templateParentPath, key), nestedTemplateParentRecord, report))
                {
                    return false;
                }
            }
            else if (!isHidden(key, record))
            {
                report.addProblem(new MissingSkeletonsProblem(path, I18N.format("inherited.missing.skeletons", key), templateParentPath, key));
                return false;
            }
        }

        return true;
    }

    private boolean isHidden(String key, Record record)
    {
        return TemplateRecord.getHiddenKeys(record).contains(key);
    }

    private void checkTemplatedCollection(String scopeName, CompositeType itemType, Record record, ConfigurationHealthReport report)
    {
        checkForSimpleKeys(scopeName, record, report);
        for (String item : record.nestedKeySet())
        {
            checkTemplatedCollectionItem(scopeName, itemType, item, report);
        }
    }

    private void checkTemplatedCollectionItem(String scopeName, CompositeType itemType, String itemKey, ConfigurationHealthReport report)
    {
        String nestedPath = getPath(scopeName, itemKey);
        // Go to the CTM so we get a templatised record.
        Record nestedRecord = configurationTemplateManager.getRecord(nestedPath);
        if (StringUtils.equals(nestedRecord.getSymbolicName(), itemType.getSymbolicName()))
        {
            checkRecord(nestedPath, itemType, nestedRecord, report);
        }
        else
        {
            report.addProblem(new UnsolvableHealthProblem(nestedPath, I18N.format("templated.item.type.mismatch", itemType.getSymbolicName(), nestedRecord.getSymbolicName())));
        }
    }

    private boolean checkRecord(String path, ComplexType type, Record record, ConfigurationHealthReport report)
    {
        if (type instanceof CollectionType)
        {
            return checkCollection(path, (CollectionType) type, record, report);
        }
        else
        {
            return checkComposite(path, (CompositeType) type, record, report);
        }
    }

    private boolean checkCollection(String path, CollectionType type, Record record, ConfigurationHealthReport report)
    {
        int problemsBefore = report.getProblemCount();

        checkForSimpleKeys(path, record, report);
        if (record instanceof TemplateRecord)
        {
            TemplateRecord templateRecord = (TemplateRecord) record;
            // Only check orders where they are defined.  Inherited orders may
            // validly contain references to items not visible at this level.
            checkCollectionOrder(path, templateRecord.getMoi(), report);
            checkHiddenItems(path, templateRecord, report);
        }
        else
        {
            checkCollectionOrder(path, record, report);
        }

        CompositeType targetType = (CompositeType) type.getTargetType();
        for (String item : record.nestedKeySet())
        {
            String nestedPath = getPath(path, item);
            Record nestedRecord = (Record) record.get(item);
            CompositeType actualType = (CompositeType) checkType(nestedPath, targetType, nestedRecord, report);
            if (actualType != null)
            {
                checkComposite(nestedPath, actualType, nestedRecord, report);
            }
        }

        return report.getProblemCount() == problemsBefore;
    }

    private void checkForSimpleKeys(String path, Record record, ConfigurationHealthReport report)
    {
        for (String key : record.simpleKeySet())
        {
            report.addProblem(new UnexpectedSimpleValueProblem(path, I18N.format("unexpected.simple.key", key), key));
        }
    }

    private void checkCollectionOrder(String path, Record record, ConfigurationHealthReport report)
    {
        List<String> orderKeys = CollectionType.getDeclaredOrder(record);
        for (String orderKey : orderKeys)
        {
            Object item = record.get(orderKey);
            if (item == null)
            {
                report.addProblem(new InvalidOrderKeyProblem(path, I18N.format("order.key.invalid", orderKey), orderKey));
            }
            else if (!(item instanceof Record))
            {
                report.addProblem(new InvalidOrderKeyProblem(path, I18N.format("order.key.refers.to.simple", orderKey), orderKey));
            }
        }
    }

    private void checkHiddenItems(String path, TemplateRecord record, ConfigurationHealthReport report)
    {
        Set<String> hiddenKeys = TemplateRecord.getHiddenKeys(record);
        TemplateRecord templateParentRecord = record.getParent();
        if (templateParentRecord == null)
        {
            if (!hiddenKeys.isEmpty())
            {
                report.addProblem(new UnexpectedHiddenKeysProblem(path, I18N.format("hidden.keys.unexpected", hiddenKeys)));
            }
        }
        else
        {
            for (String hiddenKey : hiddenKeys)
            {
                if (record.getMoi().containsKey(hiddenKey))
                {
                    report.addProblem(new InvalidHiddenKeyProblem(path, I18N.format("hidden.key.exists", hiddenKey), hiddenKey));
                }

                Object value = templateParentRecord.get(hiddenKey);
                if (value == null)
                {
                    report.addProblem(new InvalidHiddenKeyProblem(path, I18N.format("hidden.key.not.in.parent", hiddenKey), hiddenKey));
                }
                else if (!(value instanceof Record))
                {
                    report.addProblem(new InvalidHiddenKeyProblem(path, I18N.format("hidden.key.refers.to.simple", hiddenKey), hiddenKey));
                }
            }
        }
    }

    private boolean checkComposite(String path, CompositeType type, Record record, ConfigurationHealthReport report)
    {
        int problemsBefore = report.getProblemCount();

        checkReferences(path, type, record, report);

        if (record instanceof TemplateRecord)
        {
            checkScrubbed(path, type, (TemplateRecord) record, report);
        }
        
        for (String key : record.simpleKeySet())
        {
            TypeProperty property = type.getProperty(key);
            if (property == null)
            {
                if (!type.getInternalPropertyNames().contains(key))
                {
                    report.addProblem(new UnexpectedSimpleValueProblem(path, I18N.format("unexpected.simple.key", key), key));
                }
            }
            else if (!isTypeSimple(property.getType()))
            {
                report.addProblem(new UnsolvableHealthProblem(path, I18N.format("complex.property.simple.value", key)));
            }
        }

        for (String key : record.nestedKeySet())
        {
            TypeProperty property = type.getProperty(key);
            if (property == null)
            {
                report.addProblem(new UnexpectedNestedRecordProblem(path, I18N.format("unexpected.nested.record", key), key));
            }
            else if (isTypeSimple(property.getType()))
            {
                report.addProblem(new UnsolvableHealthProblem(path, I18N.format("simple.property.complex.value", key)));
            }
            else
            {
                String nestedPath = getPath(path, key);
                Record nestedRecord = (Record) record.get(key);
                ComplexType nestedType = (ComplexType) property.getType();

                ComplexType actualType = checkType(nestedPath, nestedType, nestedRecord, report);
                if (actualType != null)
                {
                    checkRecord(nestedPath, actualType, nestedRecord, report);
                }
            }
        }

        // check that collection types have the expected base record.
        for (String key : type.getNestedPropertyNames())
        {
            Type propertyType = type.getPropertyType(key);
            if (propertyType instanceof CollectionType && !record.containsKey(key))
            {
                report.addProblem(new MissingCollectionProblem(path, I18N.format("collection.missing", key), key));
            }
        }

        return report.getProblemCount() == problemsBefore;
    }

    private boolean isTypeSimple(Type type)
    {
        if (type instanceof SimpleType)
        {
            return true;
        }
        else
        {
            return type instanceof CollectionType && type.getTargetType() instanceof SimpleType;
        }
    }

    private void checkReferences(String path, CompositeType type, Record record, ConfigurationHealthReport report)
    {
        for (TypeProperty property : type.getProperties(ReferenceType.class))
        {
            ReferenceType referenceType = (ReferenceType) property.getType();
            String handleString = (String) record.get(property.getName());
            if (handleString != null)
            {
                checkReference(path, record, property, referenceType, handleString, report);
            }
        }

        for (TypeProperty property : type.getProperties(CollectionType.class))
        {
            CollectionType collectionType = (CollectionType) property.getType();
            if (collectionType.getTargetType() instanceof ReferenceType)
            {
                ReferenceType referenceType = (ReferenceType) collectionType.getTargetType();
                String[] value = (String[]) record.get(property.getName());
                if (value != null)
                {
                    for (String handleString : value)
                    {
                        checkReference(path, record, property, referenceType, handleString, report);
                    }
                }
            }
        }
    }

    private void checkReference(String path, Record record, TypeProperty property, ReferenceType referenceType, String handleString, ConfigurationHealthReport report)
    {
        try
        {
            long toHandle = referenceType.getHandle(handleString);
            if (toHandle != 0)
            {
                String handlePath = recordManager.getPathForHandle(toHandle);
                if (handlePath == null)
                {
                    report.addProblem(new InvalidReferenceProblem(path, I18N.format("reference.handle.unknown", property.getName()), property.getName(), handleString));
                }
                else if (record instanceof TemplateRecord)
                {
                    String templateOwnerPath = PathUtils.getPrefix(path, 2);
                    String referencedPath = referenceType.getReferencedPath(templateOwnerPath, handleString);
                    if (recordManager.select(referencedPath) == null)
                    {
                        // This is tricky to solve, as at the level where the
                        // handle is defined (somewhere up the hierarchy) it is
                        // valid.  Fixing it at this inherited level runs into
                        // the problem that it may not appear in this record.
                        // It is possible to fix this, but currently we do not
                        // support it.
                        report.addProblem(new UnsolvableHealthProblem(path, I18N.format("reference.cannot.push.down", property.getName())));
                    }
                    else
                    {
                        long canonicalHandle = configurationReferenceManager.getReferenceHandleForPath(templateOwnerPath, referencedPath);
                        if (toHandle != canonicalHandle)
                        {
                            report.addProblem(new NonCanonicalReferenceProblem(path, I18N.format("reference.not.canonical", property.getName()), property.getName(), handleString, Long.toString(canonicalHandle)));
                        }
                    }
                }
            }
        }
        catch (TypeException e)
        {
            report.addProblem(new InvalidReferenceProblem(path, I18N.format("reference.handle.invalid", property.getName()), property.getName(), handleString));
        }
    }

    private void checkScrubbed(String path, ComplexType type, TemplateRecord templateRecord, ConfigurationHealthReport report)
    {
        Record moi = templateRecord.getMoi();
        TemplateRecord templateParent = templateRecord.getParent();
        TemplateRecord emptyChild = new TemplateRecord(null, templateParent, type, type.createNewRecord(false));

        for (String key: moi.simpleKeySet())
        {
            Object value = moi.get(key);
            Object inheritedValue = emptyChild.get(key);
            if (RecordUtils.valuesEqual(value, inheritedValue))
            {
                report.addProblem(new NonScrubbedSimpleValueProblem(path, I18N.format("simple.value.not.scrubbed", key), key, inheritedValue));
            }
        }
    }

    private ComplexType checkType(String path, ComplexType expectedType, Record record, ConfigurationHealthReport report)
    {
        String symbolicName = record.getSymbolicName();
        if (expectedType instanceof CollectionType)
        {
            if (symbolicName != null)
            {
                report.addProblem(new UnsolvableHealthProblem(path, I18N.format("type.mismatch.expected.collection", symbolicName)));
                return null;
            }

            return expectedType;
        }
        else if (typeRegistry.getType(symbolicName) == null)
        {
            report.addProblem(new UnsolvableHealthProblem(path, I18N.format("type.invalid", symbolicName)));
            return null;
        }
        else
        {
            try
            {
                return configurationTemplateManager.typeCheck((CompositeType) expectedType, symbolicName);
            }
            catch (IllegalArgumentException e)
            {
                report.addProblem(new UnsolvableHealthProblem(path, I18N.format("type.mismatch.composites", expectedType.getSymbolicName(), symbolicName)));
                return null;
            }
        }
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
