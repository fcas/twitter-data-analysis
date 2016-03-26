package ufrn.imd.engsoft.model;

import java.io.Serializable;

/**
 * Created by Felipe on 3/26/16.
 */
public class Metrics implements Serializable
{
    private double _mean;
    private double _max;
    private double _min;
    private double _median;
    private double _standardDeviation;
    private double _variance;

    public Metrics() {}

    public void setMean(double average)
    {
        _mean = average;
    }

    public double getMax()
    {
        return _max;
    }

    public void setMax(double max)
    {
        _max = max;
    }

    public double getMin()
    {
        return _min;
    }

    public void setMin(double min)
    {
        _min = min;
    }

    public double getMedian()
    {
        return _median;
    }

    public void setMedian(double median)
    {
        _median = median;
    }

    public double getStandardDeviation()
    {
        return _standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation)
    {
        _standardDeviation = standardDeviation;
    }

    public double getVariance()
    {
        return _variance;
    }

    public void setVariance(double variance)
    {
        _variance = variance;
    }
}
