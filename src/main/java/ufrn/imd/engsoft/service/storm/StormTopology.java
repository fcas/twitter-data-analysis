package ufrn.imd.engsoft.service.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import ufrn.imd.engsoft.service.helpers.TwitterKeysReader;
import java.util.Properties;

/**
 * Created by Felipe on 10/8/15.
 */

public class StormTopology {

    private static Config _config;

    public static void main(String[] args)
    {
        TopologyBuilder builder = new TopologyBuilder();

        setConfig();

        Properties properties = TwitterKeysReader.getTwitterKeys();

        builder.setSpout("twitter", new TwitterStreamSpout
        (
            properties.getProperty("consumerKey"),
            properties.getProperty("consumerSecret"),
            properties.getProperty("accessToken"),
            properties.getProperty("accessTokenSecret")), 8
        );

        builder.setBolt("tweetCleaner", new TweetCleanerBolt()).shuffleGrouping("twitter");
        builder.setBolt("symbolsCleaner", new SymbolsCleanerBolt()).shuffleGrouping("tweetCleaner");
        builder.setBolt("tokenizer", new TokenizerBolt()).shuffleGrouping("symbolsCleaner");
        builder.setBolt("tags", new TaggerBolt()).shuffleGrouping("tokenizer");
        builder.setBolt("sentimentAnalyser", new SentimentAnalyserBolt()).shuffleGrouping("tags");
        builder.setBolt("persisting", new PersistingBolt()).shuffleGrouping("sentimentAnalyser");

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("twitterTopology", _config, builder.createTopology());

        try
        {
            Thread.sleep(60000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        cluster.killTopology("twitterTopology");
        cluster.shutdown();
        System.exit(0);

//       try {
//            // This statement submit the topology on remote cluster.
//            // args[0] = name of topology
//            StormSubmitter.submitTopology(args[0], _config,
//                    builder.createTopology());
//        }catch(AlreadyAliveException alreadyAliveException) {
//            System.out.println(alreadyAliveException);
//        } catch
//                (InvalidTopologyException invalidTopologyException) {
//            System.out.println(invalidTopologyException);
//        }
    }

    private static void setConfig()
    {
        _config = new Config();
        _config.setDebug(true);
        _config.setNumWorkers(12);
    }
}
