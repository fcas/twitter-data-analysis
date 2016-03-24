package ufrn.imd.engsoft.stormAnalysis;

/**
 * Created by Felipe on 10/8/15.
 */

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import twitter4j.Status;

public class WordsSplitting extends BaseBasicBolt {

    private static final long serialVersionUID = 5856179550908037438L;

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        String[] words = ((Status)tuple.getValue(0)).getText().split(" ");
        for (int i = 0; i < words.length; i++){
            String word = words[i];
            if (!word.isEmpty()){
                basicOutputCollector.emit(new Values(word));
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }

}
