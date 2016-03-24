package ufrn.imd.engsoft.stormAnalysis;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;

/**
 * Created by Felipe on 10/8/15.
 */

public class StormTopology {

    private static Config conf;

    public static void main(String[] args) {

        TopologyBuilder builder = new TopologyBuilder();
        setConfig();

        builder.setSpout("twitter", new Twitter(args[0], args[1], args[2], args[3]), 1);
        builder.setBolt("wordSplitting", new WordsSplitting()).shuffleGrouping("twitter");
        builder.setBolt("wordFormatting", new WordsFormatting()).shuffleGrouping("wordSplitting");
        builder.setBolt("wordPersisting", new WordsPersisting()).shuffleGrouping("wordFormatting");

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("twitterTopology", conf, builder.createTopology());

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cluster.killTopology("twitterTopology");
        cluster.shutdown();
        System.exit(0);
    }

    private static void setConfig() {
        conf = new Config();
        conf.setDebug(false);
    }

}
