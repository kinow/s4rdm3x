package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapper;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.Random;

public class NBMapperExperimentRunner extends ExperimentRunner {

    private ExperimentRunData.NBMapperData m_exData;

    RandomBoolVariable m_doStemming;
    RandomBoolVariable m_doWordCount;
    RandomDoubleVariable m_threshold;


    public NBMapperExperimentRunner(System a_sua, Metric a_metric) {
        super (a_sua, a_metric);
        m_doStemming = new RandomBoolVariable(false);
        m_doWordCount = new RandomBoolVariable(false);
        m_threshold = new RandomDoubleVariable(0.9, 0);
    }

    @Override
    protected ExperimentRunData.BasicRunData createNewRunData(Random a_rand) {
        m_exData = new ExperimentRunData.NBMapperData();
        m_exData.m_threshold = m_threshold.generate(a_rand);
        m_exData.m_doStemming = m_doStemming.generate(a_rand);
        m_exData.m_doWordCount = m_doWordCount.generate(a_rand);
        return m_exData;
    }

    @Override
    protected boolean runClustering(CGraph a_g, FanInCache fic, ArchDef arch) {
        NBMapper c = new NBMapper(arch);
        long start = java.lang.System.nanoTime();
        c.run(a_g);
        m_exData.m_time = java.lang.System.nanoTime() - start;

        m_exData.m_totalManuallyClustered += c.m_manuallyMappedNodes;
        m_exData.m_totalAutoClustered += c.m_automaticallyMappedNodes;
        m_exData.m_totalAutoWrong  += c.m_autoWrong;
        m_exData.m_totalFailedClusterings  += c.m_failedMappings;

        if (c.m_automaticallyMappedNodes + c.m_manuallyMappedNodes == 0) {
            return true;
        }

        m_exData.m_iterations++;
        return true;
    }
}