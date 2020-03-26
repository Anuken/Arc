package arc.math;

/**
 * A simple class keeping track of the mean of a stream of values within a certain window. the WindowedMean will only return a
 * value in case enough data has been sampled. After enough data has been sampled the oldest sample will be replaced by the newest
 * in case a new sample is added.
 * @author badlogicgames@gmail.com
 */
public final class WindowedMean{
    float[] values;
    int addedValues = 0;
    int lastValue;
    float mean = 0;
    boolean dirty = true;

    /**
     * constructor, windowSize specifies the number of samples we will continuously get the mean and variance from. the class
     * will only return meaning full values if at least windowSize values have been added.
     * @param windowSize size of the sample window
     */
    public WindowedMean(int windowSize){
        values = new float[windowSize];
    }

    /** @return whether the value returned will be meaningful */
    public boolean hasEnoughData(){
        return addedValues >= values.length;
    }

    /** clears this WindowedMean. The class will only return meaningful values after enough data has been added again. */
    public void clear(){
        addedValues = 0;
        lastValue = 0;
        for(int i = 0; i < values.length; i++)
            values[i] = 0;
        dirty = true;
    }

    /**
     * adds a new sample to this mean. In case the window is full the oldest value will be replaced by this new value.
     * @param value The value to add
     */
    public void addValue(float value){
        if(addedValues < values.length) addedValues++;
        values[lastValue++] = value;
        if(lastValue > values.length - 1) lastValue = 0;
        dirty = true;
    }

    /**
     * returns the mean of the samples added to this instance. Only returns meaningful results when at least window_size samples
     * as specified in the constructor have been added.
     * @return the mean
     */
    public float getMean(){
        if(hasEnoughData()){
            if(dirty){
                float mean = 0;
                for(int i = 0; i < values.length; i++)
                    mean += values[i];

                this.mean = mean / values.length;
                dirty = false;
            }
            return this.mean;
        }else return 0;
    }

    /** @return the oldest value in the window */
    public float getOldest(){
        return addedValues < values.length ? values[0] : values[lastValue];
    }

    /** @return the value last added */
    public float getLatest(){
        return values[lastValue - 1 == -1 ? values.length - 1 : lastValue - 1];
    }

    /** @return The standard deviation */
    public float standardDeviation(){
        if(!hasEnoughData()) return 0;

        float mean = getMean();
        float sum = 0;
        for(int i = 0; i < values.length; i++){
            sum += (values[i] - mean) * (values[i] - mean);
        }

        return (float)Math.sqrt(sum / values.length);
    }

    public float getLowest(){
        float lowest = Float.MAX_VALUE;
        for(int i = 0; i < values.length; i++)
            lowest = Math.min(lowest, values[i]);
        return lowest;
    }

    public float getHighest(){
        float lowest = Float.MIN_NORMAL;
        for(int i = 0; i < values.length; i++)
            lowest = Math.max(lowest, values[i]);
        return lowest;
    }

    public int getValueCount(){
        return addedValues;
    }

    public int getWindowSize(){
        return values.length;
    }

    /**
     * @return A new <code>float[]</code> containing all values currently in the window of the stream, in order from oldest to
     * latest. The length of the array is smaller than the window size if not enough data has been added.
     */
    public float[] getWindowValues(){
        float[] windowValues = new float[addedValues];
        if(hasEnoughData()){
            for(int i = 0; i < windowValues.length; i++){
                windowValues[i] = values[(i + lastValue) % values.length];
            }
        }else{
            System.arraycopy(values, 0, windowValues, 0, addedValues);
        }
        return windowValues;
    }
}
