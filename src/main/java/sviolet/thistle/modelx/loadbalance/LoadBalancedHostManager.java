package sviolet.thistle.modelx.loadbalance;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class LoadBalancedHostManager {

    private AtomicInteger counter = new AtomicInteger(0);

    private AtomicReference<Host[]> hostArray = new AtomicReference<>(new Host[0]);
    private Map<String, Integer> hostIndexMap = new HashMap<>(0);

    @Nullable
    public Host nextHost(){

        Host[] hostArray = this.hostArray.get();

        if (hostArray.length <= 0){
            return null;
        } else if (hostArray.length == 1){
            return hostArray[0];
        }

        Host host;

        int count = 0;
        int reCountTimes = hostArray.length >> 1 + 1;
        long currentTimeMillis = System.currentTimeMillis();

        for (int i = 0 ; i < reCountTimes ; i++) {
            count = counter.getAndIncrement() % hostArray.length;
            host = hostArray[count];
            if (!host.isBlocked(currentTimeMillis)){
                return host;
            }
        }

        for (int i = 0 ; i < hostArray.length - 1 ; i++){
            count = ++count % hostArray.length;
            host = hostArray[count];
            if (!host.isBlocked(currentTimeMillis)){
                return host;
            }
        }

        return hostArray[++count % hostArray.length];

    }

    /*****************************************************************************************************************
     * settings
     */

    private AtomicInteger settingCounter = new AtomicInteger(0);
    private AtomicReference<List<String>> newSettings = new AtomicReference<>(null);

    public void setHosts(List<String> hosts){
        if (hosts == null){
            hosts = new ArrayList<>(0);
        }

        for (int i = 0 ; i < hosts.size() ; i++) {
            if (hosts.get(i) == null){
                hosts.remove(i);
                i--;
            }
        }

        newSettings.set(hosts);

        int count = settingCounter.incrementAndGet();
        if (count > 1){
            settingCounter.decrementAndGet();
            return;
        }

        List<String> newSettings;
        while ((newSettings = this.newSettings.getAndSet(null)) != null){

            Host[] hostArray = this.hostArray.get();

            int newSize = newSettings.size();
            Host[] newHostArray = new Host[newSize];
            Map<String, Integer> newHostIndexMap = new HashMap<>(newSize);

            for (int i = 0 ; i < newSize ; i++){

                String newUrl = newSettings.get(i);
                Integer oldIndex = hostIndexMap.get(newUrl);

                if (oldIndex != null){
                    try {
                        newHostArray[i] = new Host(newUrl, hostArray[oldIndex].blockingTime);
                    } catch (Throwable ignore){
                        newHostArray[i] = new Host(newUrl, new AtomicLong(0));
                    }
                } else {
                    newHostArray[i] = new Host(newUrl, new AtomicLong(0));
                }

                newHostIndexMap.put(newUrl, i);

            }

            this.hostArray.set(newHostArray);
            hostIndexMap = newHostIndexMap;

        }

        settingCounter.decrementAndGet();
    }

    public static class Host {

        private String url;
        private AtomicLong blockingTime;

        private Host(String url, AtomicLong blockingTime) {
            this.url = url;
            this.blockingTime = blockingTime;
        }

        public String getUrl() {
            return url;
        }

        public void block(long duration){
            blockingTime.set(System.currentTimeMillis() + duration);
        }

        private boolean isBlocked(long currentTimeMillis){
            return currentTimeMillis < blockingTime.get();
        }

    }

}
