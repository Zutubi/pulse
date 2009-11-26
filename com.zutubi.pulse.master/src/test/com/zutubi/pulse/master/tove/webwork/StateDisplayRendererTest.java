package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.format.StateDisplayManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.TreeNode;
import org.mockito.Matchers;
import static org.mockito.Matchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;

import static java.util.Arrays.asList;

public class StateDisplayRendererTest extends PulseTestCase
{
    private static final String DUMMY_FIELD = "field";

    private StateDisplayRenderer renderer;
    private StateDisplayManager stateDisplayManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        stateDisplayManager = Mockito.mock(StateDisplayManager.class);
        renderer = new StateDisplayRenderer();
        renderer.setStateDisplayManager(stateDisplayManager);
    }

    public void testRenderString()
    {
        setupFormat("a string");
        assertEquals("a string", renderer.render(DUMMY_FIELD, null));
    }

    public void testRenderObject()
    {
        setupFormat(new ToString());
        assertEquals(ToString.STRING_VALUE, renderer.render(DUMMY_FIELD, null));
    }

    public void testRenderHTMLEscaped()
    {
        setupFormat("<tag>");
        assertEquals("&lt;tag&gt;", renderer.render(DUMMY_FIELD, null));
    }

    public void testRenderCollection()
    {
        setupFormat(asList("1", "2", "3"));
        assertEquals("<ul><li>1</li><li>2</li><li>3</li></ul>",
                renderer.render(DUMMY_FIELD, null));
    }

    public void testRenderCollectionOnLimit()
    {
        setupFormat(asList("1", "2", "3", "4"));
        assertEquals("<ul><li>1</li><li>2</li><li>3</li><li>4</li></ul>",
                renderer.render(DUMMY_FIELD, null));
    }

    public void testRenderCollectionJustOverLimit()
    {
        setupFormat(asList("1", "2", "3", "4", "5"));
        assertEquals("<ul class='top-level' onclick='toggleStateList(event);'><li>1</li><li>2</li><li>3</li><li class='excess'>4</li><li class='excess'>5</li><li class='details expansion' id='state.field.expand'/>... 2 more items (click to expand)</li><li class='details excess' id='state.field.collapse'/>all 5 items shown (click to collapse)</li></ul>",
                renderer.render(DUMMY_FIELD, null));
    }

    public void testRenderCollectionWellOverLimit()
    {
        setupFormat(asList("1", "2", "3", "4", "5", "6", "7"));
        assertEquals("<ul class='top-level' onclick='toggleStateList(event);'><li>1</li><li>2</li><li>3</li><li class='excess'>4</li><li class='excess'>5</li><li class='excess'>6</li><li class='excess'>7</li><li class='details expansion' id='state.field.expand'/>... 4 more items (click to expand)</li><li class='details excess' id='state.field.collapse'/>all 7 items shown (click to collapse)</li></ul>",
                renderer.render(DUMMY_FIELD, null));
    }

    public void testRenderTree()
    {
        setupFormat(new TreeNode<String>("root",
                new TreeNode<String>("leaf1"),
                new TreeNode<String>("middle",
                        new TreeNode<String>("leaf2"))));
        assertEquals("<ul><li>leaf1</li><li>middle<ul><li>leaf2</li></ul></li></ul>",
                renderer.render(DUMMY_FIELD, null));
    }

    public void testRenderTreeOnLimit()
    {
        setupFormat(
                new TreeNode<String>("0",
                        new TreeNode<String>("00L"),
                        new TreeNode<String>("01",
                                new TreeNode<String>("010L")),
                        new TreeNode<String>("02L")));
        assertEquals(
                "<ul>" +
                        "<li>00L</li>" +
                        "<li>01<ul>" +
                                "<li>010L</li>" +
                        "</ul></li>" +
                        "<li>02L</li>" +
                "</ul>",
                renderer.render(DUMMY_FIELD, null));
    }
    
    public void testRenderTreeOverLimit()
    {
        setupFormat(
                new TreeNode<String>("0",
                        new TreeNode<String>("00L"),
                        new TreeNode<String>("01",
                                new TreeNode<String>("010L"),
                                new TreeNode<String>("011L")),
                        new TreeNode<String>("02L")));
        assertEquals(
                "<ul class='top-level' onclick='toggleStateList(event);'>" +
                        "<li>00L</li>" +
                        "<li>01<ul>" +
                                "<li>010L</li>" +
                                "<li class='excess'>011L</li>" +
                        "</ul></li>" +
                        "<li class='excess'>02L</li>" +
                        "<li class='details expansion' id='state.field.expand'/>... 2 more items (click to expand)</li>" +
                        "<li class='details excess' id='state.field.collapse'/>all 5 items shown (click to collapse)</li>" +
                "</ul>",
                renderer.render(DUMMY_FIELD, null));
    }

    private void setupFormat(Object formatted)
    {
        doReturn(formatted).when(stateDisplayManager).format(anyString(), Matchers.<Configuration>anyObject());
    }

    private static class ToString
    {
        private static final String STRING_VALUE = "tostring";

        @Override
        public String toString()
        {
            return STRING_VALUE;
        }
    }
}
