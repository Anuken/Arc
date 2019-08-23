package io.anuke.mnet;

import io.anuke.arc.collection.*;

public class DefaultCongestionManager implements CongestionManager {

    private final double minimalAcceptablePacketLoss;
    private final long maxDelay;
    private final int recalculateTime;
    private int resendPackets;
    private LongArray noResendsDelays = new LongArray();

    /**
     * Optimal values for {@link #DefaultCongestionManager(double, long, int)} constructor
     */
    public DefaultCongestionManager() {
        this(0.05, 2000, 100);
    }

    /**
     * @param minimalAcceptablePacketLoss how much of packet loss is considered acceptable and requires no action.
     * @param maxDelay Max delay that can ever be in case when no packets are delivered without resending.
     * @param recalculateTime how often to do recalculation.
     */
    public DefaultCongestionManager(double minimalAcceptablePacketLoss, long maxDelay, int recalculateTime) {
        this.minimalAcceptablePacketLoss = Math.max(0, minimalAcceptablePacketLoss);
        this.maxDelay = Math.max(16, maxDelay);
        this.recalculateTime = Math.max(10, recalculateTime);
    }

    @Override
    public long calculateDelay(MSocketImpl.ResendPacket respondedPacket, long currentTime, long currentDelay) {
        if (respondedPacket.resends == 0) {
            noResendsDelays.add(currentTime - respondedPacket.sendTime);
        } else {
            resendPackets++;
        }
        if (resendPackets + noResendsDelays.size > recalculateTime){
            long newDelay = calculateDelay(currentDelay);
            resendPackets = 0;
            noResendsDelays.clear();
            return newDelay;
        }

        return currentDelay;
    }

    private long calculateDelay(long currentDelay){
        long newDelay;
        if (noResendsDelays.size * minimalAcceptablePacketLoss >= resendPackets){ // most of the packets are delivered.
            newDelay = (long) Math.ceil(standardDeviationMax());

        } else if (noResendsDelays.size * 0.90 > resendPackets){ // pretty much no packets arrived without resending
            newDelay = (long) Math.min((currentDelay * 1.5), maxDelay); //Never go higher than maxDelay at this point. dangerous.

        } else {
            double sdm = standardDeviationMax();
            if (sdm * 1.5 > currentDelay){ // We're on edge. It's most likely is that ping became a little bigger and we need to adjust.
                newDelay = (long) (currentDelay * 1.1);
            } else {
                newDelay = (long) sdm;
                //Didn't matter how high we'd rise, packet loss is still high, but the data that arrives
                //is still pretty fast.
                //we decide to lower resend time back to standard deviation maximum.
                //1. If packet loss is actually higher than minimal acceptable value, then it will help us with
                //correct resending time.
                //2. If It's a mistake and real ping is higher, we will get there eventually
                //at a cost of a congestion right now.
            }
        }
        return newDelay > 16 ? newDelay : 16; //Меньше 16 не возвращаем не надёжно.
    }

    //mean + 2xSigma
    //mean value plus 2 standard deviations. more than 97% of values will be less than that.
    private double standardDeviationMax(){
        int size = noResendsDelays.size;
        long[] values = noResendsDelays.items;

        double mean = 0;
        for (int i = 0; i < size; i++) {
            mean += values[i];
        }
        mean = mean / size;

        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += (values[i] - mean) * (values[i] - mean);
        }

        return mean + (2 * Math.sqrt(sum / size));
    }

}
