package arc.audio;

//TODO
public class Filter{
    long handle;

    public Filter(FilterType type){

    }

    public enum FilterType{
        biquad,
        echo,
        lofi,
        flanger,
        fft,
        bassboost,
        waveshaper,
        robotize,
        freeverb
    }
}
