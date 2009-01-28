package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Content;
import com.zutubi.pulse.core.engine.api.Property;
import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ToveFileLoaderTest extends PulseTestCase
{
    private static final String EXTENSION = "xml";

    private TypeRegistry registry;
    private ToveFileLoader loader;
    private CompositeType rootType;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        registry = new TypeRegistry();
        rootType = registry.register(SimpleRoot.class);

        loader = new ToveFileLoader();
        loader.setTypeRegistry(registry);
        loader.setObjectFactory(new DefaultObjectFactory());
        loader.setValidationManager(new PulseValidationManager());
        loader.register("reference", registry.getType(SimpleReference.class));
        loader.register("referrer", registry.getType(Referrer.class));
        loader.register("collection-referrer", registry.getType(CollectionReferrer.class));
        loader.register("textual", registry.getType(Textual.class));
        loader.register("property", registry.register(Property.class));
    }

    public void testSimpleReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput(EXTENSION), root);

        SimpleReference t = root.getReferences().get("a");
        assertNotNull(t);
        assertEquals("a", t.getName());
    }

    public void testResolveReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput(EXTENSION), root);

        Referrer er = root.getReferrers().get("er");
        assertNotNull(er);

        SimpleReference ee = er.getRef();
        assertNotNull(ee);
        assertEquals("ee", ee.getName());
    }

    public void testCollectionReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput(EXTENSION), root);

        CollectionReferrer er = root.getCollectionReferrers().get("er");
        assertNotNull(er);

        assertEquals(1, er.getRefs().size());
        SimpleReference ee = er.getRefs().get(0);
        assertEquals("ee", ee.getName());
    }

    public void testContentProperty() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput(EXTENSION), root);

        Textual textual = root.getTextual();
        assertNotNull(textual);
        assertEquals("text content v", textual.getX());
    }

    @SymbolicName("simpleRoot")
    public static class SimpleRoot extends AbstractConfiguration
    {
        private Map<String, SimpleReference> references = new HashMap<String, SimpleReference>();
        private Map<String, Referrer> referrers = new HashMap<String, Referrer>();
        private Map<String, CollectionReferrer> collectionReferrers = new HashMap<String, CollectionReferrer>();
        private Textual textual;

        public Map<String, SimpleReference> getReferences()
        {
            return references;
        }

        public void setReferences(Map<String, SimpleReference> references)
        {
            this.references = references;
        }

        public Map<String, Referrer> getReferrers()
        {
            return referrers;
        }

        public void setReferrers(Map<String, Referrer> referrers)
        {
            this.referrers = referrers;
        }

        public Map<String, CollectionReferrer> getCollectionReferrers()
        {
            return collectionReferrers;
        }

        public void setCollectionReferrers(Map<String, CollectionReferrer> collectionReferrers)
        {
            this.collectionReferrers = collectionReferrers;
        }

        public Textual getTextual()
        {
            return textual;
        }

        public void setTextual(Textual textual)
        {
            this.textual = textual;
        }
    }

    @SymbolicName("simpleReference")
    public static class SimpleReference extends AbstractNamedConfiguration implements Reference
    {
        public Object referenceValue()
        {
            return this;
        }
    }

    @SymbolicName("referrer")
    public static class Referrer extends AbstractNamedConfiguration
    {
        private SimpleReference ref;

        public SimpleReference getRef()
        {
            return ref;
        }

        public void setRef(SimpleReference ref)
        {
            this.ref = ref;
        }
    }

    @SymbolicName("collectionReferrer")
    public static class CollectionReferrer extends AbstractNamedConfiguration
    {
        @com.zutubi.tove.annotations.Reference @Addable(value = "el", reference = "at")
        private List<SimpleReference> refs = new LinkedList<SimpleReference>();

        public List<SimpleReference> getRefs()
        {
            return refs;
        }

        public void setRefs(List<SimpleReference> refs)
        {
            this.refs = refs;
        }
    }

    @SymbolicName("textual")
    public static class Textual extends AbstractConfiguration
    {
        @Content
        private String x;

        public String getX()
        {
            return x;
        }

        public void setX(String x)
        {
            this.x = x;
        }
    }
}
