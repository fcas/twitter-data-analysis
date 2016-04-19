package ufrn.imd.engsoft.service.storm;

/**
 * Created by Felipe on 10/8/15.
 */

import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class TwitterStreamSpout extends BaseRichSpout
{
    private SpoutOutputCollector _collector;
    private LinkedBlockingQueue<Status> _queue = null;
    private String _consumerKey;
    private String _consumerSecret;
    private String _accessToken;
    private String _accessTokenSecret;
    private AccessToken _token;
    private Twitter twitter;
    private String[] _keyWords = {"pecesiqueira", "naosalvo", "rafinhabastos", "jovemnerd", "mkarolqueiroz", "felipenetoreal", "belpesce", "dilma", "cunha"};
    private StatusListener _listener;
    private TwitterStream _twitterStream;

    public TwitterStreamSpout(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret)
    {
        _consumerKey = consumerKey;
        _consumerSecret = consumerSecret;
        _accessToken = accessToken;
        _accessTokenSecret = accessTokenSecret;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector)
    {
        _queue = new LinkedBlockingQueue<>(1000);
        _collector = collector;

        _listener = new StatusListener()
        {
            @Override
            public void onStatus(Status status)
            {
                _queue.offer(status);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn)
            {
            }

            @Override
            public void onTrackLimitationNotice(int i)
            {
            }

            @Override
            public void onScrubGeo(long l, long l1)
            {
            }

            @Override
            public void onException(Exception ex)
            {
            }

            @Override
            public void onStallWarning(StallWarning arg0)
            {
            }
        };

        twitterAuthentication();
        trackStream();
    }

    public void trackStream()
    {
        _twitterStream = new TwitterStreamFactory(
                new ConfigurationBuilder().setJSONStoreEnabled(true).build())
                .getInstance();

        _twitterStream.addListener(_listener);
        _twitterStream.setOAuthConsumer(_consumerKey, _consumerSecret);
        _twitterStream.setOAuthAccessToken(_token);

        FilterQuery query = new FilterQuery().track(_keyWords);
        _twitterStream.filter(query);
    }

    public void twitterAuthentication()
    {
        _token = new AccessToken(_accessToken, _accessTokenSecret);
        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(_consumerKey, _consumerSecret);
        twitter.setOAuthAccessToken(_token);
    }

    @Override
    public void nextTuple()
    {
        Status ret = _queue.poll();
        if (ret == null)
        {
            Utils.sleep(50);
        }
        else
        {
            _collector.emit(new Values(ret));
        }
    }

    @Override
    public void close()
    {
        try
        {
            _twitterStream.cleanUp();
            _twitterStream.shutdown();
        }
        catch (Exception e)
        {
        }
    }

    @Override
    public Map<String, Object> getComponentConfiguration()
    {
        Config ret = new Config();
        ret.setMaxTaskParallelism(1);
        return ret;
    }

    @Override
    public void ack(Object id)
    {
    }

    @Override
    public void fail(Object id)
    {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweet"));
    }
}
