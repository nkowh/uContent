package com.nikoyo.ansj;


import org.elasticsearch.common.inject.AbstractModule;

public class AnsjIndicesAnalysisModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AnsjIndicesAnalysis.class).asEagerSingleton();
    }
}
