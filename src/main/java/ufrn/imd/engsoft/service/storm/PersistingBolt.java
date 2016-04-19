package ufrn.imd.engsoft.service.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.bson.Document;
import twitter4j.UserMentionEntity;
import ufrn.imd.engsoft.dao.TweetsDAO;
import ufrn.imd.engsoft.model.TweetStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Felipe on 4/18/16.
 */

public class PersistingBolt extends BaseRichBolt {

    private static TweetsDAO _tweetsDAO;
    private static String _collectionName = "twitter-stream";
    private List<String> _usersMention;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        _tweetsDAO = TweetsDAO.getInstance(_collectionName, false);
    }

    @Override
    public void execute(Tuple tuple)
    {
        TweetStream tweetStream = (TweetStream) tuple.getValue(0);
        Document document = new Document();
        document.put("_id", tweetStream.getId());
        document.put("_sentiment", tweetStream.getSentiment());

        _usersMention = new ArrayList<>();

        for (UserMentionEntity userMention : tweetStream.getUsersMention())
        {
            _usersMention.add(userMention.getScreenName());
        }

        document.put("_userMentions", _usersMention);
        _tweetsDAO.saveTweetStreams(document);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
    {
    }

    @Override
    public void cleanup()
    {
        _tweetsDAO.closeMongo();
    }
}