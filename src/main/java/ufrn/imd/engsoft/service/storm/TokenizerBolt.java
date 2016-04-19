package ufrn.imd.engsoft.service.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import ufrn.imd.engsoft.model.TweetStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Felipe on 10/24/15.
 */

public class TokenizerBolt extends BaseRichBolt
{
    private String _tokenModelFileName = "pt-token.bin";
    private InputStream _inputStream = null;
    private OutputCollector _outputCollector;
    private TokenizerModel _model;
    private Tokenizer _tokenizer;
    private List<String> _tokenList;
    private List<String> _stopwords;
    private HashMap<String, String> _abbreviationsMap;
    private ClassLoader _classLoader;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        _classLoader = getClass().getClassLoader();

        String abbreviationsFileName = "abbreviations.txt";
        String stopwordsFilename = "stopwords.txt";

        List<String> abbreviationsFileLines;
        _inputStream = _classLoader.getResourceAsStream(_tokenModelFileName);

        if (_inputStream != null)
        {
            try
            {
                _model = new TokenizerModel(_inputStream);
                _tokenizer = new TokenizerME(_model);
                abbreviationsFileLines = Files.readAllLines(getResourceUri(abbreviationsFileName));
                _abbreviationsMap = linesToMap(abbreviationsFileLines);
                _stopwords = Files.readAllLines(getResourceUri(stopwordsFilename));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        _outputCollector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
        TweetStream tweetStream = (TweetStream) tuple.getValue(0);
        String tweet = tweetStream.getSymbolLessTweetText();
        String tokens[] = _tokenizer.tokenize(tweet);
        _tokenList = new ArrayList<>(Arrays.asList(tokens));

        replaceAbbreviations();
        removeStopWords();

        tweetStream.setTokenList(_tokenList);
        _outputCollector.emit(new Values(tweetStream));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tokens"));
    }

    @Override
    public void cleanup()
    {
        try
        {
            _inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void replaceAbbreviations()
    {
        for (int i = 0; i < _tokenList.size(); i++)
        {
            String token = _tokenList.get(i);
            if (_abbreviationsMap.containsKey(token))
            {
               _tokenList.set(i, _abbreviationsMap.get(token));
            }
        }
    }

    private void removeStopWords()
    {
        _stopwords.stream()
                .filter(string -> _tokenList.contains(string))
                .forEach(string -> _tokenList.remove(string));
    }

    private Path getResourceUri(String filename)
    {
        try
        {
            URI uri = _classLoader.getResource(filename).toURI();
            return Paths.get(uri);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    private HashMap<String, String> linesToMap(List<String> lines)
    {
        HashMap<String, String> map = new HashMap<>();
        for(String line : lines)
        {
            map.put(line.split(",")[0], line.split(",")[1]);
        }
        return map;
    }
}