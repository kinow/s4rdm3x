package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NBMapperTests {

    @Test
    public void getDependencyStringFromNode() {
        NodeGenerator ng = new NodeGenerator();
        ArchDef a = new ArchDef();
        ArchDef.Component c1, c2;

        c1 = a.addComponent("Component1");
        c2 = a.addComponent("Component2");
        CGraph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"AB", "BC", "CA", "DC", "AC"});
        c1.mapToNode(g.getNode("A"));
        c1.clusterToNode(g.getNode("A"), ArchDef.Component.ClusteringType.Initial);

        c1.mapToNode(g.getNode("B"));
        c1.clusterToNode(g.getNode("B"), ArchDef.Component.ClusteringType.Initial);

        c1.mapToNode(g.getNode("C"));   // this should be component 1 for the mapping and component 2 for the clustering
        c2.clusterToNode(g.getNode("C"), ArchDef.Component.ClusteringType.Automatic);


        IRMapperBase sut = new NBMapper(null, false, false, false, false, 0, 0.9);

        ArrayList<MapperBase.ClusteredNode> nodes = new ArrayList<>();

        nodes.add(new MapperBase.ClusteredNode(g.getNode("A"), a));
        nodes.add(new MapperBase.ClusteredNode(g.getNode("B"), a));
        nodes.add(new MapperBase.ClusteredNode(g.getNode("C"), a));


        try {
            Method sutMethod = IRMapperBase.class.getDeclaredMethod("getDependencyStringFromNode", CNode.class, Iterable.class);
            sutMethod.setAccessible(true);

            String expected = "Component1ImplementsComponent1 Component1ImplementsComponent2";
            String actual = (String)sutMethod.invoke(sut, g.getNode("A"), nodes);
            assertEquals(expected, actual);

            expected = "Component1ImplementsComponent2";
            actual = (String)sutMethod.invoke(sut, g.getNode("B"), nodes);
            assertEquals(expected, actual);

            expected = "Component2ImplementsComponent1";
            actual = (String)sutMethod.invoke(sut, g.getNode("C"), nodes);
            assertEquals(expected, actual);

        } catch (Exception e) {
            assertEquals(true, false);
        }

        /*String expected = "Component1ImplementsComponent1 Component1ImplementsComponent2";
        String actual = sut.getDependencyStringFromNode(g.getNode("A"), g.getNodes());

        assertEquals(expected, actual);

        expected = "Component1ImplementsComponent2";
        actual = sut.getDependencyStringFromNode(g.getNode("B"), g.getNodes());
        assertEquals(expected, actual);

        expected = "Component2ImplementsComponent1";
        actual = sut.getDependencyStringFromNode(g.getNode("C"), g.getNodes());
        assertEquals(expected, actual);*/
    }

    @Test
    public void getDependencyStringToNode() {
        NodeGenerator ng = new NodeGenerator();
        ArchDef a = new ArchDef();
        ArchDef.Component c1, c2;

        c1 = a.addComponent("Component1");
        c2 = a.addComponent("Component2");
        CGraph g = ng.generateGraph(dmDependency.Type.LocalVar, new String [] {"AB", "BC", "CA", "DC", "AC"});
        c1.mapToNode(g.getNode("A"));
        c1.clusterToNode(g.getNode("A"), ArchDef.Component.ClusteringType.Initial);

        c1.mapToNode(g.getNode("B"));
        c1.clusterToNode(g.getNode("B"), ArchDef.Component.ClusteringType.Initial);

        c1.mapToNode(g.getNode("C"));   // this should be component 1 for the mapping and component 2 for the clustering
        c2.clusterToNode(g.getNode("C"), ArchDef.Component.ClusteringType.Automatic);


        IRMapperBase sut = new NBMapper(null, false, false, false, false, 0, 0.9);

        ArrayList<MapperBase.ClusteredNode> nodes = new ArrayList<>();

        nodes.add(new MapperBase.ClusteredNode(g.getNode("A"), a));
        nodes.add(new MapperBase.ClusteredNode(g.getNode("B"), a));
        nodes.add(new MapperBase.ClusteredNode(g.getNode("C"), a));

        try {
            Method sutMethod = IRMapperBase.class.getDeclaredMethod("getDependencyStringToNode", CNode.class, Iterable.class);
            sutMethod.setAccessible(true);

            String expected = "Component2LocalVarComponent1";
            String actual = (String)sutMethod.invoke(sut, g.getNode("A"), nodes);
            assertEquals(expected, actual);

            expected = "Component1LocalVarComponent1";
            actual = (String)sutMethod.invoke(sut, g.getNode("B"), nodes);
            assertEquals(expected, actual);

            expected = "Component1LocalVarComponent2 Component1LocalVarComponent2";
            actual = (String)sutMethod.invoke(sut, g.getNode("C"), nodes);
            assertEquals(expected, actual);

        } catch (Exception e) {
            assertEquals(true, false);
        }
    }

    @Test
    void getTrainingData() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "BC", "CA", "DC", "AC", "AB"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");
        CNode c = g.getNode("C");
        CNode d = g.getNode("D");   // this is the orphan

        ArchDef arch = new ArchDef();
        ArchDef.Component c1 = arch.addComponent("Component1");
        ArchDef.Component c2 = arch.addComponent("Component2");
        c1.clusterToNode(a, ArchDef.Component.ClusteringType.Initial);
        c1.mapToNode(a);
        c1.clusterToNode(b, ArchDef.Component.ClusteringType.Initial);
        c1.mapToNode(b);
        c2.clusterToNode(c, ArchDef.Component.ClusteringType.Initial);
        c2.mapToNode(c);

        c2.mapToNode(d);

        NBMapper sut = new NBMapper(arch, true, true, true, false, 0, 0.9);
        StringToWordVector filter = new StringToWordVector();
        filter.setOutputWordCounts(true);

        Instances actual = sut.getTrainingData(sut.getInitiallyMappedNodes(g), arch, filter, null);

        System.out.println(actual);
        Attribute classAttribute = actual.classAttribute();

        for (Instance inst : actual) {

            Enumeration<Attribute> attribs = actual.enumerateAttributes();

            System.out.print("class: " + classAttribute.value((int)inst.value(classAttribute)) + " ");
            while (attribs.hasMoreElements()) {
                Attribute attr = attribs.nextElement();
                //if (attr.isNumeric()) {
                    System.out.print(attr.name() + ":" + inst.value(attr));
                //} else {
                    //System.out.print(attr.name() + ":" + inst.stringValue(attr));
                //}
            }
            System.out.println("");
            //inst.stringValue()


        }
    }

    @Test
    void runTest() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "BC", "CA", "DC", "AC"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");
        CNode c = g.getNode("C");
        CNode d = g.getNode("D");   // this is the orphan

        ArchDef arch = new ArchDef();
        ArchDef.Component c1 = arch.addComponent("Component1");
        ArchDef.Component c2 = arch.addComponent("Component2");
        c1.clusterToNode(a, ArchDef.Component.ClusteringType.Initial);
        c1.mapToNode(a);
        c1.clusterToNode(b, ArchDef.Component.ClusteringType.Initial);
        c1.mapToNode(b);
        c2.clusterToNode(c, ArchDef.Component.ClusteringType.Initial);
        c2.mapToNode(c);


        c1.mapToNode(d);


        NBMapper sut = new NBMapper(arch, true, true, true, false, 0, 0.9);

        sut.run(g);

    }

    @Test
    void deCamelCaseTest() {
        NBMapper sut = new NBMapper(null, true, true, true, false, 0, 0.9);

        assertEquals("test", sut.deCamelCase("test", 3, null));
        assertEquals("test test", sut.deCamelCase("testTest", 3, null));
        assertEquals("test", sut.deCamelCase("Test", 3, null));
        assertEquals("test test", sut.deCamelCase("TestTest", 3, null));
        assertEquals("test test", sut.deCamelCase("TestTEST", 3, null));
        assertEquals("test test", sut.deCamelCase("Test_TEST", 3, null));
        assertEquals("test test", sut.deCamelCase("test_test", 3, null));
        assertEquals("test test", sut.deCamelCase("test-test", 3, null));
        assertEquals("test test test", sut.deCamelCase("test-testTest", 3, null));
        assertEquals("test", sut.deCamelCase("test-te", 3, null));
        assertEquals("", sut.deCamelCase("teTe", 3, null));
        assertEquals("test test test testing", sut.deCamelCase("testTest testTesting", 3, null));

        assertEquals("test test test test", sut.deCamelCase("testTest testTesting", 3, new weka.core.stemmers.SnowballStemmer()));
    }

    @Test
    void getMaxIndicesTest() {
        NBMapper sut = new NBMapper(null, true, true, true, false, 0, 0.9);

        assertEquals(0, sut.getMaxIndices(new double[]{1, 0})[0]);
        assertEquals(1, sut.getMaxIndices(new double[]{1, 0})[1]);
        assertEquals(1, sut.getMaxIndices(new double[]{0, 1})[0]);
        assertEquals(0, sut.getMaxIndices(new double[]{0, 1})[1]);
        assertEquals(3, sut.getMaxIndices(new double[]{0, 1, 2, 4, 3})[0]);
        assertEquals(4, sut.getMaxIndices(new double[]{0, 1, 2, 4, 3})[1]);
        assertEquals(3, sut.getMaxIndices(new double[]{3, 1, 2, 4, 4})[0]);
        assertEquals(0, sut.getMaxIndices(new double[]{3, 1, 2, 4})[1]);
        assertEquals(4, sut.getMaxIndices(new double[]{3, 1, 2, 4, 4})[1]);
        assertEquals(1, sut.getMaxIndices(new double[]{3, 5, 2, 4, 4})[0]);
        assertEquals(3, sut.getMaxIndices(new double[]{3, 5, 2, 4, 4})[1]);
    }
}
