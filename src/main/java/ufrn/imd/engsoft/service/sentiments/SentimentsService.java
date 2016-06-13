package ufrn.imd.engsoft.service.sentiments;

import com.google.common.collect.Lists;
import ufrn.imd.engsoft.dao.ITweetsDAO;
import ufrn.imd.engsoft.dao.TweetsDAO;
import ufrn.imd.engsoft.service.fusionTables.FusionTablesService;
import ufrn.imd.engsoft.service.fusionTables.IFusionTablesService;
import ufrn.imd.engsoft.service.helpers.CitiesReader;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Felipe on 26/05/16.
 */
@Path("/smartcity")
public class SentimentsService implements ISentimentsService
{
    private IFusionTablesService _fusionTablesService;
    private ITweetsDAO _tweetsDAO;
    private int _positiveSentimentCount;
    private int _negativeSentimentCount;
    private int _neutralSentimentCount;
    private int _requestCounter;

    public SentimentsService()
    {
        _fusionTablesService = new FusionTablesService();
    }

    @POST
    @Path("/sentiments")
    public Response processMetrics()
    {
        Iterator iterator = CitiesReader.getCities().entrySet().iterator();
        do
        {
            Map.Entry pair = (Map.Entry)iterator.next();
            getSentiments(pair.getValue().toString(), pair.getKey().toString());
            iterator.remove(); // avoids a ConcurrentModificationException
        } while (iterator.hasNext());

        return Response.status(Response.Status.OK).build();
    }

    private void getSentiments(String username, String federativeUnit)
    {
        _tweetsDAO = TweetsDAO.getInstance("spark_twitter_stream", false);
        _positiveSentimentCount = Lists.newArrayList(_tweetsDAO.getSentiments(username, "positive").iterator()).size();
        _negativeSentimentCount = Lists.newArrayList(_tweetsDAO.getSentiments(username, "negative").iterator()).size();
        _neutralSentimentCount = Lists.newArrayList(_tweetsDAO.getSentiments(username, "neutral").iterator()).size();
        _fusionTablesService.updateSentiments(_positiveSentimentCount, _negativeSentimentCount, _neutralSentimentCount, "spark", federativeUnit);
        _tweetsDAO.closeMongo();

        _tweetsDAO = TweetsDAO.getInstance("storm_twitter_stream", false);
        _positiveSentimentCount = Lists.newArrayList(_tweetsDAO.getSentiments(username, "positive").iterator()).size();
        _negativeSentimentCount = Lists.newArrayList(_tweetsDAO.getSentiments(username, "negative").iterator()).size();
        _neutralSentimentCount = Lists.newArrayList(_tweetsDAO.getSentiments(username, "neutral").iterator()).size();
        _fusionTablesService.updateSentiments(_positiveSentimentCount, _negativeSentimentCount, _neutralSentimentCount, "storm", federativeUnit);

        _requestCounter += 8;

        if (_requestCounter == 24)
        {
            try
            {
                Thread.sleep(60000);
                _requestCounter = 0;
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
