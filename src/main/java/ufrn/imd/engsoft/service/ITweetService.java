package ufrn.imd.engsoft.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Created by Felipe on 3/13/16.
 */
public interface ITweetService
{
    @GET
    @Path("/metrics/{username}")
    @Produces("application/json")
    void processUserTimeLine(@PathParam("username") String username);
}
