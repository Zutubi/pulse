package com.zutubi.tove.config.health;

import com.zutubi.tove.config.ConfigurationPersistenceManager;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationScopeInfo;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.StringUtils;

import java.util.List;
import java.util.Set;

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
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private RecordManager recordManager;

    /**
     * Run all checks over the entire record store, reporting any problems
     * found.
     *
     * @return a report listing all problems found
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
     */
    public ConfigurationHealthReport checkPath(String path)
    {        
        String[] elements = PathUtils.getPathElements(path);
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
                    if (templateParentRecord == null || checkStructuresMatch(path, record.getMoi(), templateParentRecord.getMoi(), report))
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

    private void checkRoot(ConfigurationHealthReport report)
    {
        Record root = recordManager.select();
        for (String key : root.simpleKeySet())
        {
            report.addProblem("", "Root record contains unexpected simple key '" + key + "'.");
        }

        for (String key : root.nestedKeySet())
        {
            ConfigurationScopeInfo scope = configurationPersistenceManager.getScopeInfo(key);
            if (scope == null)
            {
                report.addProblem("", "Root record contains unexpected nested record '" + key + "' (no matching scope registered).");
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
            String path = PathUtils.getPath(scopeName, item);
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
                        report.addProblem(scopeName, "Scope has multiple roots '" + rootItem + "' and '" + item + "'.");
                        return false;
                    }

                    if (!configurationTemplateManager.isTemplate(record))
                    {
                        report.addProblem(path, "Scope has a root '" + item + "' not marked as a template.");
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

        String path = PathUtils.getPath(scopeName, itemKey);
        Record record = (Record) templateCollectionRecord.get(itemKey);
        if (checkType(path, type, record, report) != null)
        {
            String parentHandleString = record.getMeta(TemplateRecord.PARENT_KEY);
            if (parentHandleString != null)
            {
                try
                {
                    long parentHandle = Long.parseLong(parentHandleString);
                    String parentPath = recordManager.getPathForHandle(parentHandle);
                    if (parentPath == null)
                    {
                        report.addProblem(path, "Unknown parent handle '" + parentHandle + "'.");
                    }
                    else
                    {
                        String[] parentPathElements = PathUtils.getPathElements(parentPath);
                        if (parentPathElements.length != 2 || !parentPathElements[0].equals(scopeName))
                        {
                            report.addProblem(path, "Parent handle references invalid path '" + parentPath + "': not an item of the same templated collection.");
                        }
                        else
                        {
                            Record templateParentRecord = recordManager.select(parentPath);
                            if (templateParentRecord == null)
                            {
                                report.addProblem(path, "Parent handle references invalid path '" + parentPath + "': path does not exist.");
                            }
                            else
                            {
                                if (!configurationTemplateManager.isTemplate(templateParentRecord))
                                {
                                    report.addProblem(path, "Parent handle references invalid path '" + parentPath + "': record is not a template.");
                                }
                                else
                                {
                                    checkStructuresMatch(path, record, templateParentRecord, report);
                                }
                            }
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    report.addProblem(path, "Illegal parent handle value '" + parentHandleString + "'.");
                }
            }
        }

        return problemsBefore == report.getProblemCount();
    }

    private boolean checkStructuresMatch(String path, Record record, Record templateParentRecord, ConfigurationHealthReport report)
    {
        for (String key : templateParentRecord.nestedKeySet())
        {
            if (record.containsKey(key))
            {
                Object nested = record.get(key);
                if (!(nested instanceof Record))
                {
                    report.addProblem(path, "Template parent contains nested record '" + key + "' which is not a record in this child.");
                    return false;
                }

                String childPath = PathUtils.getPath(path, key);
                Record nestedRecord = (Record) nested;
                Record nestedTemplateParentRecord = (Record) templateParentRecord.get(key);
                if (!StringUtils.equals(nestedRecord.getSymbolicName(), nestedTemplateParentRecord.getSymbolicName()))
                {
                    report.addProblem(childPath, "Type does not match template parent: this type '" + nestedRecord.getSymbolicName() + "', parent type '" + nestedTemplateParentRecord.getSymbolicName() + "'.");
                    return false;
                }

                if (!checkStructuresMatch(path, nestedRecord, nestedTemplateParentRecord, report))
                {
                    return false;
                }
            }
            else if (!isHidden(key, record))
            {
                report.addProblem(path, "Template parent contains nested record '" + key + "' not present or hidden in this child.");
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
        String nestedPath = PathUtils.getPath(scopeName, itemKey);
        // Go to the CTM so we get a templatised record.
        Record nestedRecord = configurationTemplateManager.getRecord(nestedPath);
        if (StringUtils.equals(nestedRecord.getSymbolicName(), itemType.getSymbolicName()))
        {
            checkRecord(nestedPath, itemType, nestedRecord, report);
        }
        else
        {
            report.addProblem(nestedPath, "Template collection item has incorrect type, expected '" + itemType.getSymbolicName() + "', got '" + nestedRecord.getSymbolicName() + "'.");
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
            String nestedPath = PathUtils.getPath(path, item);
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
            report.addProblem(path, "Unexpected simple key '" + key + "'.");
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
                report.addProblem(path, "Order contains reference to unknown item '" + orderKey + "'.");
            }
            else if (!(item instanceof Record))
            {
                report.addProblem(path, "Order contains reference to simple key '" + orderKey + "'.");

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
                report.addProblem(path, "Hidden keys " + hiddenKeys + " found when there is no template parent.");
            }
        }
        else
        {
            for (String hiddenKey : hiddenKeys)
            {
                if (record.containsKey(hiddenKey))
                {
                    report.addProblem(path, "Hidden key '" + hiddenKey + "' exists in this record.");
                }

                Object value = templateParentRecord.get(hiddenKey);
                if (value == null)
                {
                    report.addProblem(path, "Hidden key '" + hiddenKey + "' does not exist in template parent.");
                }
                else if (!(value instanceof Record))
                {
                    report.addProblem(path, "Hidden key '" + hiddenKey + "' does not refer to a record");
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
                    report.addProblem(path, "Record contains unrecognised key '" + key + "'.");
                }
            }
            else if (!isTypeSimple(property.getType()))
            {
                report.addProblem(path, "Simple value found at key '" + key + "' but corresponding property has complex type.");
            }
        }

        for (String key : record.nestedKeySet())
        {
            TypeProperty property = type.getProperty(key);
            if (property == null)
            {
                report.addProblem(path, "Record contains unrecognised key '" + key + "'.");
            }
            else if (isTypeSimple(property.getType()))
            {
                report.addProblem(path, "Nested record found at key '" + key + "' but corresponding property has simple type.");
            }
            else
            {
                String nestedPath = PathUtils.getPath(path, key);
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
                report.addProblem(path, "Expected nested record for key '" + key + "' was missing.");
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
                    report.addProblem(path, "Broken reference for property '" + property.getName() + "': raw handle does not exist.");
                }
                else if (record instanceof TemplateRecord)
                {
                    String templateOwnerPath = PathUtils.getPrefix(path, 2);
                    String referencedPath = referenceType.getReferencedPath(templateOwnerPath, handleString);
                    if (recordManager.select(referencedPath) == null)
                    {
                        report.addProblem(path, "Broken reference for property '" + property.getName() + "': path is invalid when pushed down.");
                    }
                    else
                    {
                        long canonicalHandle = configurationReferenceManager.getReferenceHandleForPath(templateOwnerPath, referencedPath);
                        if (toHandle != canonicalHandle)
                        {
                            report.addProblem(path, "Reference for property '" + property.getName() + "' is not pulled up to highest level.");
                        }
                    }
                }
            }
        }
        catch (TypeException e)
        {
            report.addProblem(path, "Getting handle for reference property '" + property.getName() + "': " + e.getMessage());
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
                report.addProblem(path, "Value of simple key '" + key + "' should be scrubbed as it is identical in the template parent.");
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
                report.addProblem(path, "Expected a collection, but got symbolic name '" + symbolicName + "'.");
                return null;
            }

            return expectedType;
        }
        else
        {
            try
            {
                return configurationTemplateManager.typeCheck((CompositeType) expectedType, symbolicName);
            }
            catch (IllegalArgumentException e)
            {
                report.addProblem(path, e.getMessage());
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
}
