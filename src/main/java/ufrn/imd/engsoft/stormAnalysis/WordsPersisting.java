package ufrn.imd.engsoft.stormAnalysis;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.mongodb.MongoClient;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Map;

/**
 * Created by Felipe on 10/24/15.
 */

public class WordsPersisting extends BaseRichBolt {

    private MongoClient mongoClient;
    private MongoDatabase database;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector){

        try{
//            mongoClient = MongoClients.create();
            database = mongoClient.getDatabase("tweets-db");
        } catch (Exception e){

        }

        dropCollection();
    }

    @Override
    public void execute(Tuple tuple) {

        insert(new Document("word", tuple.getValue(0)));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public void cleanup(){
        close();
    }

    public void insert(Document document){
//        if(!document.isEmpty()){
//            database.getCollection("stormWords").insertOne(document, new SingleResultCallback<Void>() {
//                @Override
//                public void onResult(Void result, Throwable t) {
//                }
//            });
//        }
    }

    public void dropCollection(){
//        database.getCollection("stormWords").drop(new SingleResultCallback<Void>() {
//            @Override
//            public void onResult(Void result, Throwable throwable) {
//            }
//        });
    }

    public void close(){
        mongoClient.close();
    }
}