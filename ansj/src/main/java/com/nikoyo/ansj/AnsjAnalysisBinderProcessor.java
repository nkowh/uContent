package com.nikoyo.ansj;

import org.ansj.lucene4.AnsjIndexAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.AnalyzerScope;


public class AnsjAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {

        analyzersBindings.processAnalyzer("ansj_index", AnsjIndexAnalyzerProvider.class);
        analyzersBindings.processAnalyzer("ansj_query", AnsjQueryAnalyzerProvider.class);
    }
}
