package ufrn.imd.engsoft.service.tweets;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Created by Felipe on 3/13/16.
 */
public interface ITweetService
{
    @POST
    @Path("/tweets")
    Response processTweets();
}