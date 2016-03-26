package ufrn.imd.engsoft.service.spark;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Created by Felipe on 3/25/16.
 */
public interface ISparkService
{
    @POST
    @Path("/metrics/{username}")
    Response processMetrics(@PathParam("username") String username);
}
