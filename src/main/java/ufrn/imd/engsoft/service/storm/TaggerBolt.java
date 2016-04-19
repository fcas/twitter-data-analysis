package ufrn.imd.engsoft.service.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import ufrn.imd.engsoft.model.TweetStream;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Felipe on 10/15/15.
 */

public class TaggerBolt extends BaseRichBolt
{
    private OutputCollector _outputCollector;
    private POSModel _model;
    private POSTaggerME _tagger;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        _model = new POSModelLoader().load(new File("src/main/resources/pt-pos-maxent.bin"));
        _tagger = new POSTaggerME(_model);
        _outputCollector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
        TweetStream tweetStream = (TweetStream) tuple.getValue(0);
        List<String> tokensList = tweetStream.getTokenList();
        String[] tokensArray = new String[tokensList.size()];
        tokensArray = tokensList.toArray(tokensArray);
        String[] tagsArray = _tagger.tag(tokensArray);
        List<String> tagsList = new ArrayList<>(Arrays.asList(tagsArray));
        POSSample sample = new POSSample(tokensList, tagsList);
        ConcurrentHashMap<String, String> _map = new ConcurrentHashMap<>();
        for (int i = 0; i < sample.getSentence().length; i++)
        {
            _map.put(sample.getSentence()[i], sample.getTags()[i]);
        }
        tweetStream.setTagsMap(_map);
        _outputCollector.emit(new Values(tweetStream));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
    {
        outputFieldsDeclarer.declare(new Fields("tags"));
    }
}