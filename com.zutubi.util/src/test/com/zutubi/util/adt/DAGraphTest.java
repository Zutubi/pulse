package com.zutubi.util.adt;

import com.zutubi.util.Mapping;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.junit.ZutubiTestCase;
import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class DAGraphTest extends ZutubiTestCase
{
    // The graph looks like:
    //     2
    // 1 <   > 4 - 5
    //     3
    private DAGraph<Integer> graph;
    private DAGraph.Node<Integer> n1;
    private DAGraph.Node<Integer> n2;
    private DAGraph.Node<Integer> n3;
    private DAGraph.Node<Integer> n4;
    private DAGraph.Node<Integer> n5;

    @Override
    protected void setUp() throws Exception
    {
        n1 = createNode(1);
        n2 = createNode(2);
        n3 = createNode(3);
        n4 = createNode(4);
        n5 = createNode(5);
        n1.connectNode(n2);
        n1.connectNode(n3);
        n2.connectNode(n4);
        n3.connectNode(n4);
        n4.connectNode(n5);
        graph = new DAGraph<Integer>(n1);
    }

    public void testFindByPredicateNotFound()
    {
        assertNull(graph.findNodeByPredicate(new DAGraph.Node.DataEqualsPredicate<Integer>(99)));
    }

    public void testFindByPredicateRoot()
    {
        assertSame(n1, graph.findNodeByPredicate(new DAGraph.Node.DataEqualsPredicate<Integer>(1)));
    }

    public void testFindByPredicateChild()
    {
        assertSame(n2, graph.findNodeByPredicate(new DAGraph.Node.DataEqualsPredicate<Integer>(2)));
    }

    public void testFindByPredicateDiamond()
    {
        assertSame(n4, graph.findNodeByPredicate(new DAGraph.Node.DataEqualsPredicate<Integer>(4)));
    }

    public void testFindByPredicateDiamondChild()
    {
        assertSame(n5, graph.findNodeByPredicate(new DAGraph.Node.DataEqualsPredicate<Integer>(5)));
    }

    public void testGetAllPathsUnknownNode()
    {
        assertEquals(0, graph.getAllPathsTo(createNode(99)).size());
    }

    public void testGetAllPathsRoot()
    {
        assertEquals(singlePathSet(), graph.getAllPathsTo(n1));
    }

    public void testGetAllPathsChild()
    {
        assertEquals(singlePathSet(n2), graph.getAllPathsTo(n2));
    }

    public void testGetAllPathsDiamond()
    {
        Set<List<DAGraph.Node<Integer>>> expected = singlePathSet(n2, n4);
        expected.add(asList(n3, n4));
        assertEquals(expected, graph.getAllPathsTo(n4));
    }

    public void testGetBuildPathDiamondChild()
    {
        Set<List<DAGraph.Node<Integer>>> expected = singlePathSet(n2, n4, n5);
        expected.add(asList(n3, n4, n5));
        assertEquals(expected, graph.getAllPathsTo(n5));
    }

    public HashSet<List<DAGraph.Node<Integer>>> singlePathSet(DAGraph.Node<Integer>... nodes)
    {
        HashSet<List<DAGraph.Node<Integer>>> result = new HashSet<List<DAGraph.Node<Integer>>>();
        result.add(asList(nodes));
        return result;
    }

    public void testForEach()
    {
        final List<DAGraph.Node<Integer>> traversalOrder = new LinkedList<DAGraph.Node<Integer>>();
        graph.forEach(new UnaryProcedure<DAGraph.Node<Integer>>()
        {
            public void run(DAGraph.Node<Integer> node)
            {
                traversalOrder.add(node);
            }
        });

        // The order of children is arbitrary for each node, so we have two possible orders
        assertTrue(traversalOrder.equals(asList(n1, n2, n4, n5, n3)) || traversalOrder.equals(asList(n1, n3, n4, n5, n2)));
    }

    public void testTransform()
    {
        DAGraph.Node<Integer> m1 = createNode(2);
        DAGraph.Node<Integer> m2 = createNode(4);
        DAGraph.Node<Integer> m3 = createNode(6);
        DAGraph.Node<Integer> m4 = createNode(8);
        DAGraph.Node<Integer> m5 = createNode(10);
        m1.connectNode(m2);
        m1.connectNode(m3);
        m2.connectNode(m4);
        m3.connectNode(m4);
        m4.connectNode(m5);
        DAGraph<Integer> expected = new DAGraph<Integer>(m1);

        DAGraph<Integer> got = graph.transform(new Mapping<Integer, Integer>()
        {
            public Integer map(Integer integer)
            {
                return integer * 2;
            }
        });

        assertEquals(expected, got);
    }

    private DAGraph.Node<Integer> createNode(int id)
    {
        return new DAGraph.Node<Integer>(id);
    }
}
