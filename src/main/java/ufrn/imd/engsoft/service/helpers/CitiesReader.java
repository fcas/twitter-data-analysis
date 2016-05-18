package ufrn.imd.engsoft.service.helpers;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Felipe on 17/05/16.
 */
public class CitiesReader
{
    public CitiesReader()
    {
    }

    public static Map<String, String> getCities()
    {
        InputStream input = CitiesReader.class.getClassLoader().getResourceAsStream("cities.json");
        InputStreamReader inputStreamReader = new InputStreamReader(input);
        JsonReader reader = new JsonReader(inputStreamReader);
        Map<String, String> cities = new HashMap<>();
        reader.setLenient(true);
        try
        {
            reader.beginObject();
            reader.nextName();
            reader.beginArray();
            while(reader.hasNext())
            {
                reader.beginObject();
                cities.put(reader.nextName(), reader.nextString());
                reader.endObject();
            }
            reader.endArray();
            reader.endObject();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return cities;
    }
}
