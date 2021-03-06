package se.lnu.siq.s4rdm3x.experiments.metric;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberOfParentsTest {

    @Test
    void test0() {
        NumberOfParents sut = new NumberOfParents();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CA"});
        CNode n1 = g.getNode("A");

        sut.assignMetric(g.getNodes());
        assertEquals(0.0, sut.getMetric(n1));
    }

    @Test
    void test1() {
        NumberOfParents sut = new NumberOfParents();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CA"});
        CNode n1 = g.getNode("C");

        sut.assignMetric(g.getNodes());
        assertEquals(1.0, sut.getMetric(n1));
    }

    @Test
    void test2() {
        NumberOfParents sut = new NumberOfParents();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"BA", "CB"});
        CNode n1 = g.getNode("C");

        sut.assignMetric(g.getNodes());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void test3() {
        NumberOfParents sut = new NumberOfParents();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"AB", "CB"});
        CNode n1 = g.getNode("C");

        sut.assignMetric(g.getNodes());
        assertEquals(1.0, sut.getMetric(n1));
    }

    @Test
    void testCircular() {
        NumberOfParents sut = new NumberOfParents();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CB", "AC"}); // circular inheritance is impossible
        CNode n1 = g.getNode("C");

        sut.assignMetric(g.getNodes());
        assertEquals(2.0, sut.getMetric(n1));
        assertEquals(2.0, sut.getMetric(g.getNode("A")));
        assertEquals(2.0, sut.getMetric(g.getNode("B")));
    }

    @Test
    void testMixedRelations() {
        NumberOfParents sut = new NumberOfParents();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"AA", "BA", "CB"});
        ng.addToGraph(g, dmDependency.Type.MethodCall, new String [] {"BA", "CB", "CB", "AC", "AA"});
        CNode n1 = g.getNode("C");

        sut.assignMetric(g.getNodes());
        assertEquals(2.0, sut.getMetric(n1));
        assertEquals(0, sut.getMetric(g.getNode("A")));
    }

    @Test
    void testMultipleClassesinSameNode() {
        NumberOfParents sut = new NumberOfParents();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"AB", "BC"});
        ng.addToGraph(g, dmDependency.Type.Extends, new String [] {"AB", "BC"});
        CNode a = g.getNode("A");

        sut.assignMetric(g.getNodes());
        assertEquals(2.0, sut.getMetric(a));
    }


    @Test
    void testSameDistantParent() {
        NumberOfParents sut = new NumberOfParents();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"AB", "BC", "AD", "DC"});
        CNode a = g.getNode("A");

        sut.assignMetric(g.getNodes());
        assertEquals(3.0, sut.getMetric(a));
    }
}
