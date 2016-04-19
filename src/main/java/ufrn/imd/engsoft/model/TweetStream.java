package ufrn.imd.engsoft.model;

import twitter4j.UserMentionEntity;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Felipe on 4/16/16.
 */
public class TweetStream implements Serializable
{
    private Long _id;
    private String _tweetText;
    private String _plainTweetText;
    private String _symbolLessTweetText;
    private String _sentiment;
    private List<String> _tokenList;
    private ConcurrentHashMap<String, String> _tagsMap;
    private UserMentionEntity[] _usersMention;

    public TweetStream()
    {
    }

    public Long getId()
    {
        return _id;
    }

    public void setId(Long id)
    {
        _id = id;
    }

    public String getTweetText()
    {
        return _tweetText;
    }

    public void setTweetText(String tweet)
    {
        _tweetText = tweet;
    }

    public String getPlainTweetText()
    {
        return _plainTweetText;
    }

    public void setPlainTweetText(String plainTweetText)
    {
        _plainTweetText = plainTweetText;
    }

    public String getSymbolLessTweetText()
    {
        return _symbolLessTweetText;
    }

    public void setSymbolLessTweetText(String symbolLessTweetText)
    {
        _symbolLessTweetText = symbolLessTweetText;
    }

    public List<String> getTokenList()
    {
        return _tokenList;
    }

    public void setTokenList(List<String> tokenList)
    {
        _tokenList = tokenList;
    }

    public ConcurrentHashMap<String, String> getTagsMap()
    {
        return _tagsMap;
    }

    public void setTagsMap(ConcurrentHashMap<String, String> tagsMap)
    {
        _tagsMap = tagsMap;
    }

    public String getSentiment()
    {
        return _sentiment;
    }

    public void setSentiment(String sentiment)
    {
        _sentiment = sentiment;
    }

    public UserMentionEntity[] getUsersMention()
    {
        return _usersMention;
    }

    public void setUsersMention(UserMentionEntity[] usersMention)
    {
        _usersMention = usersMention;
    }
}
