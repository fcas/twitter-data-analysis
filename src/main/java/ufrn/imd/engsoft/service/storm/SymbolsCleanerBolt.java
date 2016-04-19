package ufrn.imd.engsoft.service.storm;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import ufrn.imd.engsoft.model.TweetStream;

/**
 * Created by Felipe on 10/15/15.
 */

public class SymbolsCleanerBolt extends BaseBasicBolt
{
    private String[] _symbols = {".","..","...",",","!","(.)","(..)","(...)","$","%","&","_","+","=","|","#","@","'","”"};

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector)
    {
        TweetStream tweetStream = (TweetStream) tuple.getValue(0);
        String tweet = tweetStream.getPlainTweetText();

        for (String symbol : _symbols)
        {
            tweet = tweet.replace(symbol, "");
        }

        tweetStream.setSymbolLessTweetText(tweet);
        basicOutputCollector.emit(new Values(tweetStream));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweetSymbolLess"));
    }
}