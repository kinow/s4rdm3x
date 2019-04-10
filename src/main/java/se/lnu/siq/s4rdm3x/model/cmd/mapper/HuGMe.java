package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;

public class HuGMe {

    private double m_filterThreshold;   // omega in paper
    private double m_violationWeight;   // psi in paper
    //private FanInCache m_fic;

    private boolean m_doManualMapping;

    protected ArchDef m_arch;

    public ArrayList<CNode> m_clusteredElements;

    public int m_consideredNodes = 0;           // all nodes that pass the filter
    public int m_automaticallyMappedNodes = 0;
    public int m_manuallyMappedNodes = 0;
    public int m_failedMappings = 0;
    public int m_autoWrong = 0;
    public int m_unmappedNodesFromStart = 0;
    public int m_mappedNodesFromStart = 0;

    public HuGMe(double a_filterThreshold, double a_violationWeight, boolean a_doManualMapping, ArchDef a_arch, FanInCache a_fic) {
        m_violationWeight = a_violationWeight;
        m_filterThreshold = a_filterThreshold;
        m_doManualMapping = a_doManualMapping;
        m_arch = a_arch;
       // m_fic = a_fic;
    }


    // in this version all a considered node has a mapping but not a clustering as we are using this in experiments (also needed for "automatic" manual mapping)
    protected java.util.ArrayList<CNode> getOrphanNodes(CGraph a_g) {

        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (m_arch.getMappedComponent(n) != null && m_arch.getClusteredComponent(n) == null) {
                ret.add(n);
            }
        }

        return ret;
    }


    public void run(CGraph a_g) {
        final String [] originalMappingTags = m_arch.getComponentNames();

        m_clusteredElements = new ArrayList<>();

        java.util.ArrayList<CNode> unmapped = getOrphanNodes(a_g);

        // create the current clusters
        java.util.ArrayList<java.util.ArrayList<CNode>> clusters = new ArrayList<>();
        for(int i = 0; i < m_arch.getComponentCount(); i++) {
            ArrayList<CNode> c = new ArrayList<>();
            clusters.add(c);
            ArchDef.Component targetComponent = m_arch.getComponent(i);

            for(CNode n : a_g.getNodes()) {
                if (m_arch.getClusteredComponent(n) == targetComponent) {
                    c.add(n);
                }
            }
            m_mappedNodesFromStart += c.size();
        }

        m_unmappedNodesFromStart = unmapped.size();

        java.util.ArrayList<CNode> candidates = new ArrayList<>(unmapped);
        for (CNode n : unmapped) {
            // count all dependencies to this class from all other unmapped classes
            int toMappedCountC = 0, totalCountC = 0;
            for (CNode otherNode : unmapped) {
                if (n != otherNode) {
                    totalCountC += n.getDependencyCount(otherNode); //m_fic.getFanIn(n, otherNode);
                    totalCountC += otherNode.getDependencyCount(n); //m_fic.getFanIn(otherNode, n);
                }
            }

            for (ArrayList<CNode> cluster : clusters) {
                for (CNode nMapped : cluster) {

                    double fromClustered = nMapped.getDependencyCount(n);//m_fic.getFanIn(nMapped, n);
                    double toClustered = n.getDependencyCount(nMapped);//m_fic.getFanIn(n, nMapped);
                    toMappedCountC += toClustered;
                    toMappedCountC += fromClustered;
                    totalCountC += fromClustered;
                    totalCountC += toClustered;
                }
            }

            double ratio = 0.0;
            if (totalCountC > 0 && toMappedCountC > 0) {
                ratio = (double) toMappedCountC / (double) totalCountC;
            } else if (toMappedCountC > 0) {
                ratio = 1.0;
            }
            
            if (ratio < m_filterThreshold) {
                candidates.remove(n);
            }
        }

        m_consideredNodes = candidates.size();

        // 3 Count the attraction to the clusters, there will be one sum for each cluster
        for (CNode n : candidates) {
            double attractions[] = new double[m_arch.getComponentCount()];
            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                // TODO: Implement weights for different types of relations
                //attractions[i] = CountAttract(n, clusters.get(i));
                attractions[i] = CountAttractP(n, i, clusters);
            }
            n.setAttractions(attractions);
        }

        // 4 Find 2 candidate sets of attractions
        //      First set is based on >= mean of all attractions
        //      Second set is based on > standard deviation of all attractions

        for (CNode n : candidates) {
            double attractions[] = n.getAttractions();
            double mean = stats.mean(attractions);
            double sd = stats.stdDev(attractions, mean);

            ArrayList<Integer> c1 = new ArrayList<>();
            ArrayList<Integer> c2 = new ArrayList<>();

            for(int i = 0; i < m_arch.getComponentCount(); i++) {
                if (attractions[i] >= mean) {
                    c2.add(i);
                }
                if (attractions[i] - mean > sd) {
                    c1.add(i);
                }
            }


            ArchDef.Component mappedC = m_arch.getMappedComponent(n);

            if (c1.size() == 1) {
                //au.addTag(n, clusterTags[c1.get(0)]);

                //Sys.out.println("Clustered to: " + g_clusterTags[c1.get(0)]);
                //au.addTag(n, ArchDef.Component.ClusteringType.Automatic.toString());
                ArchDef.Component clusteredComponent = m_arch.getComponent(c1.get(0));
                clusteredComponent.clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
                m_clusteredElements.add(n);
                if (mappedC != clusteredComponent) {
                    m_autoWrong++;
                }
                m_automaticallyMappedNodes++;
            } else if (c2.size() == 1) {
                ArchDef.Component clusteredComponent = m_arch.getComponent(c2.get(0));
                clusteredComponent.clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
                //Sys.out.println("Clustered to: " + g_clusterTags[c2.get(0)]);
                //au.addTag(n, ArchDef.Component.ClusteringType.Automatic.toString());
                m_clusteredElements.add(n);
                if (mappedC != clusteredComponent) {
                    m_autoWrong++;
                }
                m_automaticallyMappedNodes++;
            } else if (m_doManualMapping) {


                // we always map to the correct cluster using the oracle
                // we count the advice as a fail if the attraction is below the median attraction of the clusters
                // this is possibly more correct in relation to the paper
                boolean clustered = false;
                ArchDef.Component targetC = m_arch.getMappedComponent(n);
                for(int i = 0; i < m_arch.getComponentCount(); i++){
                    if (m_arch.getComponent(i) == targetC) {
                        clustered = attractions[i] > stats.medianUnsorted(attractions);
                        ArchDef.Component.ClusteringType type = clustered ? ArchDef.Component.ClusteringType.Manual : ArchDef.Component.ClusteringType.ManualFailed;
                        targetC.clusterToNode(n, type);
                        m_manuallyMappedNodes++;
                        break;
                    }
                }







                // this is the old conservative manual mapping
                // no clear answer so we must ask the oracle...
                // i.e. if the original mapping is present in one of the available options...
                /*boolean clustered = false;
                if (c1.size() > 0) {
                    for(Integer i : c1) {
                        if (mappedC == m_arch.getComponent(i)) {
                            mappedC.clusterToNode(n, ArchDef.Component.ClusteringType.Manual);
                            clustered = true;
                            //Sys.out.println("Clustered by Oracle to: " + g_clusterTags[i]);
                            m_manuallyMappedNodes++;
                            break;
                        }
                    }
                }
                if (!clustered && c2.size() > 0) {
                    for (Integer i : c2) {
                        if (mappedC == m_arch.getComponent(i)) {
                            mappedC.clusterToNode(n, ArchDef.Component.ClusteringType.Manual);
                            //Sys.out.println("Clustered by Oracle to: " + g_clusterTags[i]);
                            m_manuallyMappedNodes++;
                            clustered = true;
                            break;
                        }
                    }
                }*/


                if (clustered == false) {
                    //Sys.out.println("No attraction... this one is dead...");
                    m_failedMappings++;
                    // we force a mapping
                    /*for(int i =0; i < g_originalMappingTags.length; i++) {
                        if (au.hasAnyTag(n, g_originalMappingTags[i])) {
                            au.addTag(n, g_clusterTags[i]);
                            au.addTag(n, "manual");
                            au.addTag(n, "forced");
                        }
                    }*/
                }
            }
        }
    }


    double CountAttractP(CNode a_node, int a_cluster, ArrayList<ArrayList<CNode>> a_clusters) {
        double overall = 0;
        double toOthers = 0;

        for (int i = 0; i < a_clusters.size(); i++) {
            final double allowedWeight = m_violationWeight;
            final double violationWeight = 1.0;
            double weightFrom, weightTo;

            if (i == a_cluster) {
                // internal dependencies are of course allowed
                weightFrom = 1.0f;
                weightTo = 1.0f;
            } else {
                ArchDef.Component from = m_arch.getComponent(a_cluster);
                ArchDef.Component to = m_arch.getComponent(i);
                // dependencies from a_cluster to other clusters (i) can incur violations
                if (!from.allowedDependency(to)) {
                    // violations have the highest weight
                    weightFrom = violationWeight;
                } else {
                    weightFrom = allowedWeight;
                }

                // dependencies from other clusters (i) to a_cluster can incur violations
                if (!to.allowedDependency(from)) {
                    weightTo = violationWeight;
                } else {
                    weightTo = allowedWeight;
                }

                toOthers += CountAttract(a_node, a_clusters.get(i), weightFrom, weightTo);

                // remember the attraction contribution to the cluster will be reduced to 0 if there are violations
                // otherwise the attraction to the cluster will increase as allowed dependencies does not reduce the attraction.
                // this also means that the overall value is the maximum attraction we can get for the
                // node and should be the number of dependencies from the node to any other mapped node
            }

            overall += CountAttract(a_node, a_clusters.get(i), 1.0, 1.0);
        }

        return overall - toOthers;
    }

    private double CountAttract(CNode a_node, Iterable<CNode> a_cluster, double a_weightFromNode, double a_weightFromCluster) {
        double count = 0;

        double cCount = 0;
        for (CNode nTo : a_cluster) {
            cCount += a_node.getDependencyCount(nTo) * a_weightFromNode; //m_fic.getFanIn(nTo, a_node) * a_weightFromNode;
            cCount += nTo.getDependencyCount(a_node) * a_weightFromCluster;//m_fic.getFanIn(a_node, nTo) * a_weightFromCluster;
        }

        count = cCount;

        return count;
    }

}