package sparkAnalysis;

import service.ITweetService;
import service.TweetService;

/**
 * Created by Felipe on 10/19/15.
 */
public class SparkStreaming {
    private static ITweetService _tweetService;

    public static void main(String[] args) {
        _tweetService = new TweetService(args[0], args[1], args[2], args[3], "Curitiba_PMC");
        _tweetService.processUserTimeLine();

        /*
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
                tweetsDAO.save(documentJavaRDD.collect());
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
        tweetsDAO.closeMongo();*/
    }
}