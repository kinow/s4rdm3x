package se.lnu.siq.s4rdm3x.model.cmd;

/**
 * Created by tohto on 2017-08-21.
 */
public class SetSpringWeight {
    private float m_newWeight;
    private String [] m_springTags;

    private SetSpringWeight(String[] a_springTags, float a_newWeight) {
        m_newWeight = a_newWeight;
        m_springTags = a_springTags;
    }

    /*public void run(Graph a_g) {
        for (Edge e: a_g.getEdgeSet()) {
            for(String tag : m_springTags) {

                tag = "[" + tag + "]";

                if (e.getId().contains(tag)) {
                    if (m_newWeight > 0.0f) {
                        e.setAttribute("layout.ignored", false);
                        e.setAttribute("layout.weight", m_newWeight);
                    } else {
                        e.setAttribute("layout.ignored", true);
                    }
                }
            }
        }
    }*/
}
