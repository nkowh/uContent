package com.nikoyo.ansj;

import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.lucene4.AnsjAnalysis;
import org.ansj.lucene4.AnsjIndexAnalysis;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;


/**
 * Registers indices level analysis components so, if not explicitly configured, will be shared
 * among all indices.
 */
public class AnsjIndicesAnalysis extends AbstractComponent {

    public final static String DEFAULT_STOP_FILE_LIB_PATH = "ansj/dic/stopLibrary.dic";
    public final static Set<String> filter = new HashSet<>();
    public final static boolean pstemming = false;

    private final Environment environment;

    @Inject
    public AnsjIndicesAnalysis(Settings settings, IndicesAnalysisService indicesAnalysisService) {
        super(settings);
        environment = new Environment(settings);
        initConfigPath(settings);
        // Register  analyzer
        indicesAnalysisService.analyzerProviderFactories().put("ansj_index", new PreBuiltAnalyzerProviderFactory("ansj_index", AnalyzerScope.INDICES, new AnsjIndexAnalysis(filter, pstemming)));
        indicesAnalysisService.analyzerProviderFactories().put("ansj_query", new PreBuiltAnalyzerProviderFactory("ansj_query", AnalyzerScope.INDICES, new AnsjAnalysis(filter, pstemming)));

        // Register  tokenizer
        indicesAnalysisService.tokenizerFactories().put("ansj_index_tokenizer", new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
            @Override
            public String name() {
                return "ansj_index_tokenizer";
            }

            @Override
            public Tokenizer create(Reader reader) {
                return new AnsjTokenizer(new IndexAnalysis(new BufferedReader(reader)), reader, filter, pstemming);
            }
        }));

        indicesAnalysisService.tokenizerFactories().put("ansj_query_tokenizer", new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
            @Override
            public String name() {
                return "ansj_query_tokenizer";
            }

            @Override
            public Tokenizer create(Reader reader) {
                return new AnsjTokenizer(new ToAnalysis(new BufferedReader(reader)), reader, filter, pstemming);
            }
        }));

        ToAnalysis.parse("一个词");
        System.out.println("ansj ready!");
    }

    private void initConfigPath(Settings settings) {
        //是否提取词干

        //pstemming = settings.getAsBoolean("pstemming", false);
        //用户自定义辞典
        File path = new File(environment.configFile(), settings.get("user_path", "ansj/dic/user"));
        MyStaticValue.userLibrary = path.getAbsolutePath();
        //用户自定义辞典
        path = new File(environment.configFile(), settings.get("ambiguity", "ansj/dic/ambiguity.dic"));
        MyStaticValue.ambiguityLibrary = path.getAbsolutePath();
        MyStaticValue.isNameRecognition = settings.getAsBoolean("is_name", true);
        MyStaticValue.isNumRecognition = settings.getAsBoolean("is_num", true);
        MyStaticValue.isQuantifierRecognition = settings.getAsBoolean("is_quantifier", true);

        try {
            File stopLibrary = new File(environment.configFile(), settings.get("stop_path", "ansj/dic/stopLibrary.dic"));
            for (String s : FileUtils.readLines(stopLibrary, "UTF-8")) {
                filter.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}