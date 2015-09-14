package com.nikoyo.ansj;



import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

import java.io.File;
import java.util.Collection;
import java.util.List;


public class Plugin extends AbstractPlugin {

    @Inject
    public Plugin() {
    }

    public String name() {
        return "Ansj";
    }

    public String description() {
        return "Ansj plugin";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        return ImmutableList.<Class<? extends Module>>of(AnsjIndicesAnalysisModule.class);
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new AnsjAnalysisBinderProcessor());
    }


}
