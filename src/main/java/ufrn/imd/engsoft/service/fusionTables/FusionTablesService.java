package ufrn.imd.engsoft.service.fusionTables;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Sqlresponse;
import ufrn.imd.engsoft.model.Fields;
import ufrn.imd.engsoft.model.Metrics;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Properties;

/**
 * Created by Felipe on 3/26/16.
 */
public class FusionTablesService
{
    private static final String _configurationFileName = "config.properties";
    private String _applicationName;
    private String _tableId;
    private FileDataStoreFactory _dataStoreFactory;
    private HttpTransport _httpTransport;
    private JsonFactory _jsonFactory = JacksonFactory.getDefaultInstance();

    private Credential _credential;

    public FusionTablesService()
    {
        try
        {
            _httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            /* Directory to store user credentials. */
            java.io.File _dataStoreDirectory = new java.io.File(System.getProperty("user.home"), ".store/fusion_tables_sample");
            _dataStoreFactory = new FileDataStoreFactory(_dataStoreDirectory);
            _credential = authorize();
            setConfigurations();
        }
        catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Credential authorize() throws Exception {

        InputStream input = getClass().getClassLoader().getResourceAsStream("client_secret.json");
        InputStreamReader inputStreamReader = new InputStreamReader(input);

        GoogleClientSecrets _clientSecrets = GoogleClientSecrets.load(_jsonFactory, inputStreamReader);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                _httpTransport, _jsonFactory, _clientSecrets,
                Collections.singleton(FusiontablesScopes.FUSIONTABLES)).setDataStoreFactory(
                _dataStoreFactory).build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    private void setConfigurations()
    {
        Properties prop = new Properties();
        InputStream input = null;

        try
        {
            input = getClass().getClassLoader().getResourceAsStream(_configurationFileName);
            if (input == null)
            {
                System.out.println("Sorry, unable to find " + _configurationFileName);
                return;
            }

            prop.load(input);

            _applicationName = prop.getProperty("applicationName");
            _tableId = prop.getProperty("tableId");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateData(Dictionary<String, Metrics> dictionary, String federativeUnit) {

        Fusiontables fusiontables = new Fusiontables.Builder(
                _httpTransport, _jsonFactory, _credential).setApplicationName(_applicationName).build();
        try
        {
            Sqlresponse result = fusiontables.query().sql(
                    "SELECT ROWID FROM " + _tableId + " WHERE UF = '" + federativeUnit + "'").execute();
            String rowId = result.getRows().get(0).get(0).toString();

            for (Fields field : Fields.values())
            {
                Metrics metrics = dictionary.get(field.name());
                fusiontables.query().sql(
                        "UPDATE " + _tableId + " SET '" + field.name() + "_mean'" + " = " + metrics.getMean() + " WHERE ROWID ='" + rowId + "'").execute();
                fusiontables.query().sql(
                        "UPDATE " + _tableId + " SET '" + field.name() + "_median'" + " = " + metrics.getMedian() + " WHERE ROWID ='" + rowId + "'").execute();
                fusiontables.query().sql(
                        "UPDATE " + _tableId + " SET '" + field.name() + "_min'" + " = " + metrics.getMin() + " WHERE ROWID ='" + rowId + "'").execute();
                fusiontables.query().sql(
                        "UPDATE " + _tableId + " SET '" + field.name() + "_max'" + " = " + metrics.getMax() + " WHERE ROWID ='" + rowId + "'").execute();
                fusiontables.query().sql(
                        "UPDATE " + _tableId + " SET '" + field.name() + "_variance'" + " = " + metrics.getVariance() + " WHERE ROWID ='" + rowId + "'").execute();
                fusiontables.query().sql(
                        "UPDATE " + _tableId + " SET '" + field.name() + "_standard_deviation'" + " = " + metrics.getStandardDeviation() + " WHERE ROWID ='" + rowId + "'").execute();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
              e.printStackTrace();
        }
    }
}
