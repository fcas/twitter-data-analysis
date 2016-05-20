package ufrn.imd.engsoft.service.spark;

import com.google.common.collect.Lists;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.DoubleFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import ufrn.imd.engsoft.dao.ITweetsDAO;
import ufrn.imd.engsoft.dao.TweetsDAO;
import ufrn.imd.engsoft.model.Fields;
import ufrn.imd.engsoft.model.Metrics;
import ufrn.imd.engsoft.model.TweetInfo;
import ufrn.imd.engsoft.model.UserInfo;
import ufrn.imd.engsoft.service.fusionTables.FusionTablesService;
import ufrn.imd.engsoft.service.fusionTables.IFusionTablesService;
import ufrn.imd.engsoft.service.helpers.CitiesReader;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Felipe on 3/25/16.
 */
@Path("/smartcity")
public class SparkService implements ISparkService, Serializable {
    private static final String _dbBaseName = "tweets_";
    private Dictionary<String, Metrics> _metrics;
    private IFusionTablesService fusionTablesService;
    private ITweetsDAO _tweetsDAO;

    @POST
    @Path("/metrics")
    public Response processMetrics()
    {
        Iterator it = CitiesReader.getCities().entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            getMetrics(pair.getValue().toString(), pair.getKey().toString());
            it.remove(); // avoids a ConcurrentModificationException
        }

        return Response.status(Response.Status.OK).build();
    }

    private void getMetrics(String username, String federativeUnit)
    {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("SparkStreamingAnalysis");
        JavaSparkContext sparkContext = new JavaSparkContext(conf);

        _tweetsDAO = TweetsDAO.getInstance(_dbBaseName + username, false);
        _metrics = new Hashtable<>();

        List<String> stringList;
        List<Long> longList;
        JavaRDD<Long> longJavaRDD;
        JavaRDD<String> stringJavaRDD;

        for (Fields field : Fields.values())
        {
            if (field != Fields._tweetCreatedAt && field != Fields._tweetCreatedAt_mention)
            {
                longList = getLongList(Lists.newArrayList(_tweetsDAO.getOrderedNumericField(field.name(), false).iterator()));
                longJavaRDD = sparkContext.parallelize(longList);
            }
            else
            {
                boolean isMention = field == Fields._tweetCreatedAt_mention;
                stringList = getStringList(Lists.newArrayList(_tweetsDAO.getOrderedNumericField(Fields._tweetCreatedAt.name(), isMention).iterator()));
                stringJavaRDD = sparkContext.parallelize(stringList);
                JavaPairRDD<String, Long> result = stringJavaRDD.mapToPair(
                        (PairFunction<String, String, Long>) x -> new Tuple2(x, (long) 1)).reduceByKey(
                        (Function2<Long, Long, Long>) (a, b) -> a + b);
                longList = result.values().takeOrdered(stringList.size());
                longJavaRDD = sparkContext.parallelize(longList);
            }

            setMetrics(longJavaRDD, longList, field.name());
        }

        UserInfo userInfo = _tweetsDAO.getUserInfo();

        fusionTablesService = new FusionTablesService();
        fusionTablesService.updateData(_metrics, userInfo, federativeUnit);
        sparkContext.close();
    }

    private List<Long> getLongList(List<TweetInfo> tweetInfoList)
    {
        List<Long> result = new ArrayList<>();
        for (TweetInfo tweetInfo : tweetInfoList)
        {
            if (tweetInfo.getInReplyToStatusId() != null)
            {
                long inReplyToStatusId = tweetInfo.getInReplyToStatusId();
                if (inReplyToStatusId == -1)
                {
                    result.add((long) 0);
                }
                else
                {
                    result.add((long) 1);
                }
            }

            if (tweetInfo.getRetweets() != null)
            {
                result.add(tweetInfo.getRetweets());
            }

            if (tweetInfo.getFavorites() != null)
            {
                result.add(tweetInfo.getFavorites());
            }

            if (tweetInfo.getResponseTime() != null)
            {
                result.add(tweetInfo.getResponseTime());
            }
        }
        return result;
    }

    private List<String> getStringList(List<TweetInfo> tweetInfoList)
    {
        return tweetInfoList.stream()
                .filter(tweetInfo -> tweetInfo.getTweetCreatedAt() != null)
                .map(TweetInfo::getTweetCreatedAt)
                .collect(Collectors.toList());
    }

    private void setMetrics(JavaRDD<Long> rdd, List<Long> longList, String fieldName)
    {
        Metrics metrics = new Metrics();

        if (!rdd.isEmpty())
        {
            JavaDoubleRDD doubleRDD = rdd.mapToDouble((DoubleFunction<Long>) value -> (double) value);
            metrics.setMean(doubleRDD.mean());
            metrics.setMax(doubleRDD.max());
            metrics.setMin(doubleRDD.min());
            metrics.setStandardDeviation(doubleRDD.stdev());
            metrics.setVariance(doubleRDD.variance());
            metrics.setMedian(getMedian(longList, longList.size()));
        }

        _metrics.put(fieldName, metrics);
    }

    private double getMedian(List<Long> numbersList, int numberOfElements)
    {
        if (numberOfElements % 2 != 0)
        {
            return numbersList.get(numberOfElements / 2);
        }
        else
        {
            int position = (numberOfElements / 2) - 1;
            return (numbersList.get(position) + numbersList.get(position + 1)) / 2;
        }
    }

}