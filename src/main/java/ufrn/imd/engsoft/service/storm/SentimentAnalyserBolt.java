package ufrn.imd.engsoft.service.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import ufrn.imd.engsoft.model.TweetStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by Felipe on 4/14/16.
 */
public class SentimentAnalyserBolt extends BaseRichBolt
{
    private JavaRDD<String> _sentiLexRdd;
    private ConcurrentHashMap<String, Integer> _sentimentMap;
    private OutputCollector _outputCollector;

    private Tuple2<String, Integer> lineToTuple(String[] strings)
    {
        String adjective = strings[0];
        int sentiment = Integer.parseInt(strings[1].split(";")[3].split("=")[1]);
        return new Tuple2<>(adjective, sentiment);
    }

    private double countSentimentsScore(int positiveSentimentsCounter, int negativeSentimentsCounter)
    {
      int sum = positiveSentimentsCounter + negativeSentimentsCounter;
      int dif = positiveSentimentsCounter - negativeSentimentsCounter;
      if (sum > 0)
      {
        return dif / sum;
      }
      else
      {
          return 0;
      }
    }

    private int emoticonCounter(String filename, String string) throws IOException
    {
        URL url = this.getClass().getClassLoader().getResource(filename);
        Path path = null;
        int emoticonCounter = 0;

        try
        {
            if (url != null)
            {
                path = Paths.get(url.toURI());
            }
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        Pattern pattern = Pattern.compile("([0-9A-Za-z'&\\-\\./\\(\\)\\]\\[<>\\}\\*\\^\"\\D=:;]+)|((?::|;|=)(?:-)?(?:\\)|D|P))");
        Matcher matcher = pattern.matcher(string);
        while(matcher.find())
        {
            String emoticon = matcher.group();
            if (path != null)
            {
                try(Stream<String> filteredLines = Files.lines(path).filter(s -> s.contains(emoticon)))
                {
                    Optional<String> hasString = filteredLines.findFirst();
                    if(hasString.isPresent())
                    {
                        emoticonCounter++;
                    }
                }
            }
        }

        return emoticonCounter;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("SparkStreamingAnalysis");
        String sentiLexFilename = "sentilex.txt";
        URL url = this.getClass().getClassLoader().getResource(sentiLexFilename);
        JavaSparkContext sparkContext = new JavaSparkContext(conf);
        try
        {
            if (url != null)
            {
                _sentiLexRdd = sparkContext.textFile(url.toURI().getPath());
            }
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        JavaPairRDD<String, Integer> sentiLexPairRdd = _sentiLexRdd.mapToPair(s -> lineToTuple(s.split(",")));
        Map<String, Integer> sentilexMap = sentiLexPairRdd.collectAsMap();
        _sentimentMap = new ConcurrentHashMap<>();
        _sentimentMap.putAll(sentilexMap);
        sparkContext.close();
        _outputCollector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
        TweetStream tweetStream = (TweetStream) tuple.getValue(0);
        ConcurrentHashMap hashMap = tweetStream.getTagsMap();
        Iterator words = hashMap.keySet().iterator();
        boolean hasNegativeAdverbs = hashMap.keySet().contains("não");
        int negativeSentimentsCounter = 0;
        int positiveSentimentsCounter = 0;
        while (words.hasNext())
        {
            String word = words.next().toString();
            String tag = hashMap.get(word).toString();
            if (tag.equals("adj"))
            {
                int sentiment = 0;
                if (_sentimentMap.containsKey(word))
                {
                    sentiment = _sentimentMap.get(word);
                }
                if (sentiment == 1)
                {
                    if (hasNegativeAdverbs)
                    {
                        negativeSentimentsCounter++;
                    }
                    else
                    {
                        positiveSentimentsCounter++;
                    }
                }
                else if (sentiment == -1)
                {
                    if (hasNegativeAdverbs)
                    {
                        positiveSentimentsCounter++;
                    }
                    else
                    {
                        negativeSentimentsCounter++;
                    }
                }
            }
        }

        String tweetText = tweetStream.getTweetText();
        try
        {
            String positiveEmoticonsFilename = "positive_emoticons.txt";
            String negativeEmoticonsFilename = "negative_emoticons.txt";
            positiveSentimentsCounter += emoticonCounter(positiveEmoticonsFilename, tweetText);
            negativeSentimentsCounter += emoticonCounter(negativeEmoticonsFilename, tweetText);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        double sentimentsScore = countSentimentsScore(positiveSentimentsCounter, negativeSentimentsCounter);

        if (sentimentsScore > 0.5)
        {
            tweetStream.setSentiment("positive");
        }
        else if (sentimentsScore != 0.0 && sentimentsScore < 0.5)
        {
            tweetStream.setSentiment("negative");
        }
        else
        {
            tweetStream.setSentiment("neutral");
        }

        _outputCollector.emit(new Values(tweetStream));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
    {
        outputFieldsDeclarer.declare(new Fields("sentiments"));
    }
}
