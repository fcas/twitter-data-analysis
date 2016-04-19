package ufrn.imd.engsoft.service.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Felipe on 4/17/16.
 */
public class TwitterKeysReader
{
    private static final String _configurationFileName = "config.properties";
    private static InputStream _input = null;
    private static TwitterKeysReader _instance;

    private TwitterKeysReader()
    {
        _input = getClass().getClassLoader().getResourceAsStream(_configurationFileName);
    }

    public static Properties getTwitterKeys()
    {
        if (_instance == null)
        {
            _instance = new TwitterKeysReader();
        }

        Properties properties = new Properties();

        try
        {
            if (_input == null)
            {
                System.out.println("Sorry, unable to find " + _configurationFileName);
                return null;
            }
            properties.load(_input);
            properties.setProperty("accessToken", properties.getProperty("accessToken"));
            properties.setProperty("accessTokenSecret", properties.getProperty("accessTokenSecret"));
            properties.setProperty("consumerKey", properties.getProperty("consumerKey"));
            properties.setProperty("consumerSecret", properties.getProperty("consumerSecret"));
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (_input != null)
            {
                try
                {
                    _input.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }
}
