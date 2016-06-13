package ufrn.imd.engsoft.service.sentiments;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Created by Felipe on 26/05/16.
 */
public interface ISentimentsService
{
    @POST
    @Path("/sentiments")
    Response processMetrics();
}
