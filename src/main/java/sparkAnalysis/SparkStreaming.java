package sparkAnalysis;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;
import org.bson.Document;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Felipe on 10/19/15.
 */

public class SparkStreaming {

    private static Twitter twitter;
    private static AccessToken token;
    private static TweetsDAO tweetsDAO;

    public static void initialization(){
        tweetsDAO = TweetsDAO.getInstance("sparkWords");
    }

    public static void main(String[] args) {

        initialization();
        authentication(args[0], args[1], args[2], args[3]);

        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("SparkStreamingAnalysis");
        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(conf, new Duration(1000));
        JavaReceiverInputDStream<Status> twitterDStream = TwitterUtils.createStream(javaStreamingContext, getTrendingTopics());

        twitterDStream.flatMap(new FlatMapFunction<Status, Document>() {
            @Override
            public Iterable<Document> call(Status status) throws Exception {
                String[] words = status.getText().split(" ");
                List<Document> documents = new ArrayList<Document>();
                for (int i = 0; i < words.length; i++) {
                    documents.add(new Document("word", words[i].trim().toLowerCase()));
                }
                return documents;
            }
        }).foreachRDD(new Function<JavaRDD<Document>, Void>() {
            @Override
            public Void call(JavaRDD<Document> documentJavaRDD) throws Exception {
                tweetsDAO.insertMany(documentJavaRDD.collect());
                return null;
            }
        });


        javaStreamingContext.start();

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        javaStreamingContext.stop();
        javaStreamingContext.close();
        tweetsDAO.closeMongo();


    }

    public static void authentication(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret){
        System.setProperty("twitter4j.oauth.consumerKey", consumerKey);
        System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret);
        System.setProperty("twitter4j.oauth.accessToken", accessToken);
        System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret);
        token = new AccessToken(accessToken, accessTokenSecret);
        twitter = new TwitterFactory().getInstance();
        try{
            twitter.setOAuthConsumer(consumerKey, consumerSecret);
            twitter.setOAuthAccessToken(token);
        } catch (Exception e){

        }

    }

    public static String[] getTrendingTopics(){

        Trends trends = null;
        try {
            trends = twitter.getPlaceTrends(1);
        } catch (TwitterException e) {
            e.printStackTrace();
        }


        String[] trendingTopics = new String[10];
        for (int i = 0; i < trends.getTrends().length; i++){
            trendingTopics[i] = trends.getTrends()[i].getName();
        }

        return trendingTopics;

    }

}