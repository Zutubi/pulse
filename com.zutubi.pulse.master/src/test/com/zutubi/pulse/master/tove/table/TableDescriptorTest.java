package com.zutubi.pulse.master.tove.table;

import com.zutubi.pulse.master.tove.model.Cell;
import com.zutubi.pulse.master.tove.model.Row;
import com.zutubi.pulse.master.tove.model.Table;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.List;

public class TableDescriptorTest extends ZutubiTestCase
{
    private ActionManager actionManager;
    private ConfigurationProvider configurationProvider;
    private TypeRegistry typeRegistry;
    private ObjectFactory objectFactory;

    private TableDescriptor descriptor;
    private CompositeType type;

    private TemplateRecord root;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        objectFactory = new DefaultObjectFactory();
        actionManager = mock(ActionManager.class);
        configurationProvider = mock(ConfigurationProvider.class);
        typeRegistry = new TypeRegistry();
        type = typeRegistry.register(SampleType.class);

        CollectionType collectionType = new MapType(type, typeRegistry);

        descriptor = new TableDescriptor(collectionType, false, false, configurationProvider, actionManager, null);
        descriptor.addColumn(column("value"));

        root = new TemplateRecord("", null, type, new MutableRecordImpl());
    }

    public void testInstantiationWithNoRows()
    {
        Record data = record();

        Table table = descriptor.instantiate("base", data);
        assertEquals(1, table.getHeaders().size());
        assertEquals(0, table.getRows().size());
    }

    public void testInstantiationWithSingleRow()
    {
        Record data = record("a");
        stub(configurationProvider.get("base/a", Configuration.class)).toReturn(new SampleType("value"));

        Table table = descriptor.instantiate("base", data);
        assertEquals(1, table.getHeaders().size());

        List<Row> rows = table.getRows();
        assertEquals(1, rows.size());
        assertRowValuesEqual(rows.get(0), "value");
    }

    public void testInstantiationWithMultipleRows()
    {
        Record data = record("a", "b", "c");
        stub(configurationProvider.get("base/a", Configuration.class)).toReturn(new SampleType("valueA"));
        stub(configurationProvider.get("base/b", Configuration.class)).toReturn(new SampleType("valueB"));
        stub(configurationProvider.get("base/c", Configuration.class)).toReturn(new SampleType("valueC"));

        Table table = descriptor.instantiate("base", data);
        assertEquals(1, table.getHeaders().size());

        List<Row> rows = table.getRows();
        assertEquals(3, rows.size());
        assertRowValuesEqual(rows.get(0), "valueA");
        assertRowValuesEqual(rows.get(1), "valueB");
        assertRowValuesEqual(rows.get(2), "valueC");
    }

    public void testInstantiationWithTemplateRow()
    {
        Record data = record("a");
        TemplateRecord template = new TemplateRecord("", root, type, data);

        stub(configurationProvider.get("base/a", Configuration.class)).toReturn(new SampleType("value"));

        Table table = descriptor.instantiate("base", template);
        assertEquals(1, table.getHeaders().size());

        List<Row> rows = table.getRows();
        assertEquals(1, rows.size());
        assertRowValuesEqual(rows.get(0), "value");
    }

    // CIB-2158
    public void testInstantiateWithStaleHiddenReferences()
    {
        MutableRecord data = record("a");
        TemplateRecord template = new TemplateRecord("", root, type, data);
        TemplateRecord.hideItem(data, "b");

        stub(configurationProvider.get("base/a", Configuration.class)).toReturn(new SampleType("value"));
        stub(configurationProvider.get("base/b", Configuration.class)).toReturn(null);

        Table table = descriptor.instantiate("base", template);
        assertEquals(1, table.getHeaders().size());

        List<Row> rows = table.getRows();
        assertEquals(2, rows.size());
        assertRowValuesEqual(rows.get(0), "value");
        assertRowValuesEqual(rows.get(1), "This collection contains a unknown hidden reference to base/b.");
    }

    private void assertRowValuesEqual(Row row, String... s)
    {
        List<Cell> cells = row.getCells();
        assertEquals(s.length, cells.size());
        for (int i = 0; i < cells.size(); i++)
        {
            assertEquals(s[i], cells.get(i).getContent());
        }
    }

    private MutableRecordImpl record(String... keys)
    {
        MutableRecordImpl record = new MutableRecordImpl();
        for (String key : keys)
        {
             // we do not require actual data here since the descriptor goes to the
            // configuration provider to retrieve the referenced instance.
            record.put(key, "<placeholder>");
        }
        return record;
    }

    private ColumnDescriptor column(String fieldName)
    {
        ColumnDescriptor descriptor = new ColumnDescriptor(fieldName, type);
        descriptor.setObjectFactory(objectFactory);
        return descriptor;
    }

    @SymbolicName("sample.type")
    public static class SampleType extends AbstractNamedConfiguration
    {
        private String value;

        public SampleType()
        {
        }

        public SampleType(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }
    }
}
