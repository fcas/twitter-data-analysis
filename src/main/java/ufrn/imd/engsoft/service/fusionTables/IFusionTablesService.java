package ufrn.imd.engsoft.service.fusionTables;

import ufrn.imd.engsoft.model.Metrics;
import ufrn.imd.engsoft.model.UserInfo;

import java.util.Dictionary;

/**
 * Created by Felipe on 3/26/16.
 */
public interface IFusionTablesService
{
    void updateData(Dictionary<String, Metrics> dictionary, UserInfo userInfo, String federativeUnit);
    void updateSentiments(int positiveCount, int negativeCount, int neutralCount, String espTool, String federativeUnit);
}
