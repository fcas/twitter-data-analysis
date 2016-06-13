package ufrn.imd.engsoft.model;

import java.util.List;
import java.util.Map;

/**
 * Created by Felipe on 26/05/16.
 */
public class Sentiment
{
    private Long _id;
    private String _sentiment;
    private String _tweetText;
    private List<String> _userMentions;
    private Map<String, String> _tags;

    public Sentiment()
    {
    }

    public Long get_id()
    {
        return _id;
    }

    public void set_id(Long _id)
    {
        this._id = _id;
    }

    public String get_sentiment()
    {
        return _sentiment;
    }

    public void set_sentiment(String _sentiment)
    {
        this._sentiment = _sentiment;
    }

    public String get_tweetText()
    {
        return _tweetText;
    }

    public void set_tweetText(String _tweetText)
    {
        this._tweetText = _tweetText;
    }

    public List<String> get_userMentions()
    {
        return _userMentions;
    }

    public void set_userMentions(List<String> _userMentions)
    {
        this._userMentions = _userMentions;
    }

    public Map<String, String> get_tags()
    {
        return _tags;
    }

    public void set_tags(Map<String, String> _tags)
    {
        this._tags = _tags;
    }
}
