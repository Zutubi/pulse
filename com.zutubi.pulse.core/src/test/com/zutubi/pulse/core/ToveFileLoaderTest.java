package com.zutubi.pulse.core;

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
        loader.register("reference", registry.getType(SimpleReference.class));
        loader.register("referrer", registry.getType(Referrer.class));
        loader.setObjectFactory(new DefaultObjectFactory());
        loader.setValidationManager(new PulseValidationManager());
    }

    public void testSimpleReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput(EXTENSION), root);

        Object o = root.getReferences().get("a");
        assertNotNull(o);
        assertTrue(o instanceof SimpleReference);

        SimpleReference t = (SimpleReference) o;
        assertEquals("a", t.getName());
    }

    public void testResolveReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput(EXTENSION), root);

        Object o = root.getReferrers().get("er");
        assertNotNull(o);
        assertTrue(o instanceof Referrer);

        Referrer er = (Referrer) o;
        SimpleReference ee = er.getRef();
        assertNotNull(ee);
        assertEquals("ee", ee.getName());
    }

    @SymbolicName("simpleRoot")
    public static class SimpleRoot extends AbstractConfiguration
    {
        private Map<String, SimpleReference> references = new HashMap<String, SimpleReference>();
        private Map<String, Referrer> referrers = new HashMap<String, Referrer>();

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
}
