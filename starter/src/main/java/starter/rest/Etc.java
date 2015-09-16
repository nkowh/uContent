package starter.rest;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import starter.RequestContext;
import starter.uContentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Etc {

    @Autowired
    private RequestContext context;

    @RequestMapping(value = "analyze", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> analyze(@RequestParam String text, @RequestParam(defaultValue = "standard") String analyzer) {
        AnalyzeResponse response = context.getClient().admin().indices().prepareAnalyze(context.getIndex(), text).setAnalyzer(analyzer).execute().actionGet();
        List<String> words = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken analyzeToken : response.getTokens()) {
            words.add(analyzeToken.getTerm());
        }
        return words;
    }
}
