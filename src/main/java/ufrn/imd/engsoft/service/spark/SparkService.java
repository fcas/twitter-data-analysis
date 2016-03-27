package ufrn.imd.engsoft.service.spark;

import com.google.common.collect.Lists;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.DoubleFunction;
import ufrn.imd.engsoft.dao.TweetsDAO;
import ufrn.imd.engsoft.model.Fields;
import ufrn.imd.engsoft.model.Metrics;
import ufrn.imd.engsoft.model.TweetInfo;
import ufrn.imd.engsoft.model.UserInfo;
import ufrn.imd.engsoft.service.fusionTables.FusionTablesService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Felipe on 3/25/16.
 */
@Path("/smartcity")
public class SparkService implements ISparkService, Serializable
{
    private static final String _dbBaseName = "tweets_";
    private static TweetsDAO _tweetsDAO;
    private Dictionary<String, Metrics> _metrics;

    @POST
    @Path("/metrics/{username}")
    public Response processMetrics(@PathParam("username") String username)
    {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("SparkStreamingAnalysis");
        JavaSparkContext sparkContext = new JavaSparkContext(conf);

        _tweetsDAO = TweetsDAO.getInstance(_dbBaseName + username, false);
        _metrics = new Hashtable<String, Metrics>();

        for(Fields field : Fields.values()){
            List<Long> longList = getLongList(Lists.newArrayList(_tweetsDAO.getOrderedNumericField(field.name()).iterator()));
            JavaRDD<Long> rdd = sparkContext.parallelize(longList);
            setMetrics(rdd, longList, field.name());
        }

        UserInfo userInfo = _tweetsDAO.getUserInfo();

        FusionTablesService fusionTablesService = new FusionTablesService();
        String federativeUnit = userInfo.getLocation().split("-")[1].trim();
        fusionTablesService.updateData(_metrics, federativeUnit);

        sparkContext.close();

        return Response.status(Response.Status.OK).build();
    }

    private List<Long> getLongList(List<TweetInfo> tweetInfoList)
    {
        List<Long> result = new ArrayList<Long>();
        for (TweetInfo tweetInfo : tweetInfoList)
        {
            if (tweetInfo.getInReplyToStatusId() != null)
            {
                long inReplyToStatusId = tweetInfo.getInReplyToStatusId();
                if (inReplyToStatusId == -1)
                {
                    result.add((long) 0);
                } else
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
        }
        return result;
    }

    private void setMetrics(JavaRDD<Long> rdd, List<Long> longList, String fieldName)
    {
        JavaDoubleRDD doubleRDD = rdd.mapToDouble(new DoubleFunction<Long>()
        {
            public double call(Long value)
            {
                return (double) value;
            }
        });

        Metrics metrics = new Metrics();
        metrics.setMean(doubleRDD.mean());
        metrics.setMax(doubleRDD.max());
        metrics.setMin(doubleRDD.min());
        metrics.setStandardDeviation(doubleRDD.stdev());
        metrics.setVariance(doubleRDD.variance());
        metrics.setMedian(getMedian(longList, longList.size()));

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
