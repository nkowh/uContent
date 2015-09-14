package com.nikoyo.ansj;

import org.ansj.lucene4.AnsjIndexAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;


public class AnsjIndexAnalyzerProvider extends AbstractIndexAnalyzerProvider<Analyzer> {
    private final Analyzer analyzer;

    @Inject
    public AnsjIndexAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                     Environment env, @Assisted String name,
                                     @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        analyzer = new AnsjIndexAnalysis(AnsjIndicesAnalysis.filter, AnsjIndicesAnalysis.pstemming);
    }

    public AnsjIndexAnalyzerProvider(Index index, Settings indexSettings, String name,
                                     Settings settings) {
        super(index, indexSettings, name, settings);
        analyzer = new AnsjIndexAnalysis(AnsjIndicesAnalysis.filter, AnsjIndicesAnalysis.pstemming);
    }

    public AnsjIndexAnalyzerProvider(Index index, Settings indexSettings, String prefixSettings,
                                     String name, Settings settings) {
        super(index, indexSettings, prefixSettings, name, settings);
        analyzer = new AnsjIndexAnalysis(AnsjIndicesAnalysis.filter, AnsjIndicesAnalysis.pstemming);
    }

    public Analyzer get() {
        return this.analyzer;
    }
}
