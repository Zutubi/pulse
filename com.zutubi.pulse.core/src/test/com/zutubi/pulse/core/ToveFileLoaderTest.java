package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Content;
import com.zutubi.pulse.core.engine.api.Property;
import com.zutubi.pulse.core.engine.api.Referenceable;
import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.*;

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
        loader.register("validateable", registry.register(SimpleValidateable.class));
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

    public void testMacroEmpty() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testMacroEmpty", "xml"), root);

        Recipe recipe = root.getRecipes().get("r1");
        assertNotNull(recipe);
        assertEquals(0, recipe.getCommands().size());
    }

    public void testMacroExpandError() throws Exception
    {
        try
        {
            load();
            fail();
        }
        catch (PulseException e)
        {
            assertThat(e.getMessage(), matchesRegex("Processing element 'macro-ref': starting at line 9 column (9|10): While expanding macro defined at line 4 column (5|6): Processing element 'no-such-type': starting at line 5 column (9|10): Unknown child element 'no-such-type'"));
        }
    }

    public void testMacroNoName() throws Exception
    {
        errorHelper("Required attribute 'name' not found");
    }

    public void testMacroUnknownAttribute() throws Exception
    {
        errorHelper("Unrecognised attribute 'unkat'");
    }

    public void testMacroRefNoMacro() throws Exception
    {
        errorHelper("Required attribute 'macro' not found");
    }

    public void testMacroRefNotMacro() throws Exception
    {
        errorHelper("Reference '${not-macro}' does not resolve to a macro");
    }

    public void testMacroRefNotFound() throws Exception
    {
        errorHelper("Unknown reference 'not-found'");
    }

    public void testMacroRefUnknownAttribute() throws Exception
    {
        errorHelper("Unrecognised attribute 'whatthe'");
    }

    public void testMacroInfiniteRecursion() throws Exception
    {
        errorHelper("Maximum recursion depth 128 exceeded");
    }

    public void testScopeTopLevel() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("xml"), root);

        assertNotNull(root.getRecipes().get("first"));
        assertNotNull(root.getRecipes().get("second"));
    }

    public void testValidateable() throws Exception
    {
        try
        {
            SimpleRoot root = new SimpleRoot();
            loader.load(getInput("xml"), root);
            fail();
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), matchesRegex("Processing element 'validateable': starting at line 4 column [56]: error\n"));
        }
    }

    public void testUnknownAttribute()
    {
        errorHelper("bad-attribute");
    }

    private SimpleRoot load() throws PulseException
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("xml"), root);
        return root;
    }

    private void errorHelper(String messageContent)
    {
        try
        {
            load();
            fail();
        }
        catch (PulseException e)
        {
            if (!e.getMessage().contains(messageContent))
            {
                fail("Message '" + e.getMessage() + "' does not contain '" + messageContent + "'");
            }
        }
    }

    @SymbolicName("simpleRoot")
    public static class SimpleRoot extends AbstractConfiguration
    {
        private Map<String, SimpleReference> references = new HashMap<String, SimpleReference>();
        private Map<String, Referrer> referrers = new HashMap<String, Referrer>();
        private Map<String, CollectionReferrer> collectionReferrers = new HashMap<String, CollectionReferrer>();
        @Addable("recipe")
        private Map<String, Recipe> recipes = new LinkedHashMap<String, Recipe>();
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

        public Map<String, Recipe> getRecipes()
        {
            return recipes;
        }

        public void setRecipes(Map<String, Recipe> recipes)
        {
            this.recipes = recipes;
        }
    }

    @SymbolicName("simpleReference")
    @Referenceable
    public static class SimpleReference extends AbstractNamedConfiguration
    {
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

    @SymbolicName("recipe")
    public static class Recipe extends AbstractNamedConfiguration
    {
        @Addable("command")
        private List<Command> commands = new LinkedList<Command>();

        public List<Command> getCommands()
        {
            return commands;
        }

        public void setCommands(List<Command> commands)
        {
            this.commands = commands;
        }
    }

    @SymbolicName("command")
    public static class Command extends AbstractNamedConfiguration
    {
    }

    @SymbolicName("validateable")
    public static class SimpleValidateable extends AbstractConfiguration implements Validateable
    {
        public void validate(ValidationContext context)
        {
            context.addFieldError("field", "error");
        }
    }
}
