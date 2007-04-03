package com.zutubi.pulse.prototype;

import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class TemplateRecordTest extends PulseTestCase
{
/*
    private TemplateRecord grandparent;
    private TemplateRecord parent;
    private TemplateRecord child;
    private TemplateRecord orphan;


    protected void setUp() throws Exception
    {
        grandparent = getEmptyTemplate("1");
        parent = getEmptyTemplate("2");
        child = getEmptyTemplate("3");

        // Simple values:
        //   gp     - in the grandparent only
        //   p      - in the parent only
        //   c      - in the child only
        //   gp p   - in the grandparent and parent
        //   gp c   - in the grandparent and child
        //   p c    - in the parent and child
        //   gp p c - in all three
        //
        // Key   = s <code from above>
        // Value = <owner code> <key>
        grandparent.put("s gp", "gpv s gp");
        parent.put("s p", "pv s p");
        child.put("s c", "cv s c");
        grandparent.put("s gp p", "gpv s gp p");
        parent.put("s gp p", "pv s gp p");
        grandparent.put("s gp c", "gpv s gp c");
        child.put("s gp c", "cv s gp c");
        parent.put("s p c", "pv s p c");
        child.put("s p c", "cv s p c");
        grandparent.put("s gp p c", "gpv s gp p c");
        parent.put("s gp p c", "pv s gp p c");
        child.put("s gp p c", "cv s gp p c");

        // List values, in a similar way, s/s/l
        // Note that lists must always be present at all levels, so we add
        // empties.
        grandparent.put("l gp", Arrays.asList("gpv l gp"));
        parent.put("l gp", Collections.emptyList());
        child.put("l gp", Collections.emptyList());

        grandparent.put("l p", Collections.emptyList());
        parent.put("l p", Arrays.asList("pv l p"));
        child.put("l p", Collections.emptyList());

        grandparent.put("l c", Collections.emptyList());
        parent.put("l c", Collections.emptyList());
        child.put("l c", Arrays.asList("cv l c"));

        grandparent.put("l gp p", Arrays.asList("gpv l gp p"));
        parent.put("l gp p", Arrays.asList("pv l gp p"));
        child.put("l gp p", Collections.emptyList());

        grandparent.put("l gp c", Arrays.asList("gpv l gp c"));
        parent.put("l gp c", Collections.emptyList());
        child.put("l gp c", Arrays.asList("cv l gp c"));

        grandparent.put("l p c", Collections.emptyList());
        parent.put("l p c", Arrays.asList("pv l p c"));
        child.put("l p c", Arrays.asList("cv l p c"));

        grandparent.put("l gp p c", Arrays.asList("gpv l gp p c"));
        parent.put("l gp p c", Arrays.asList("pv l gp p c"));
        child.put("l gp p c", Arrays.asList("cv l gp p c"));

        parent.linkToParent(grandparent);
        child.linkToParent(parent);

        // All by myself ... don't wanna be ...
        orphan = getEmptyTemplate("4");
        orphan.put("simple", "simple value");
    }

    public void testSimpleValue()
    {
        assertEquals("simple value", orphan.get("simple"));
    }

    public void testSimpleValueGP()
    {
        assertEquals("gpv s gp", child.get("s gp"));
    }

    public void testSimpleValueP()
    {
        assertEquals("pv s p", child.get("s p"));
    }

    public void testSimpleValueC()
    {
        assertEquals("cv s c", child.get("s c"));
    }

    public void testSimpleValueGP_P()
    {
        assertEquals("pv s gp p", child.get("s gp p"));
    }

    public void testSimpleValueGP_C()
    {
        assertEquals("cv s gp c", child.get("s gp c"));
    }

    public void testSimpleValueP_C()
    {
        assertEquals("cv s p c", child.get("s p c"));
    }

    public void testSimpleValueGP_P_C()
    {
        assertEquals("cv s gp p c", child.get("s gp p c"));
    }

    public void testListValueGP()
    {
        assertList(child.get("l gp"), "gpv l gp");
    }

    public void testListValueP()
    {
        assertList(child.get("l p"), "pv l p");
    }

    public void testListValueC()
    {
        assertList(child.get("l c"), "cv l c");
    }

    public void testListValueGP_P()
    {
        assertList(child.get("l gp p"), "gpv l gp p", "pv l gp p");
    }

    public void testListValueGP_C()
    {
        assertList(child.get("l gp c"), "gpv l gp c", "cv l gp c");
    }

    public void testListValueP_C()
    {
        assertList(child.get("l p c"), "pv l p c", "cv l p c");
    }

    public void testListValueGP_P_C()
    {
        assertList(child.get("l gp p c"), "gpv l gp p c", "pv l gp p c", "cv l gp p c");
    }

    private TemplateRecord getEmptyTemplate(String owner)
    {
        return new TemplateRecord(new SingleRecord("sym"), owner);
    }

    private void assertList(Object value, String... expected)
    {
        assertEquals(Arrays.asList(expected), value);
    }
*/
}
