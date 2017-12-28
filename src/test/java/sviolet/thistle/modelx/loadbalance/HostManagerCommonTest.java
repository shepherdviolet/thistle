package sviolet.thistle.modelx.loadbalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HostManagerCommonTest {

//    private static final int HOST_NUM = 1;
//    private static final int HOST_NUM = 2;
//    private static final int HOST_NUM = 4;
    private static final int HOST_NUM = 16;
//    private static final int HOST_NUM = 64;

//    private static final int TASK_NUM = 4;
    private static final int TASK_NUM = 32;

    public static void main(String[] args) {

        final Map<String, AtomicInteger> counters = new HashMap<>(HOST_NUM);
        final List<String> hosts = new ArrayList<>(HOST_NUM);
        for (int i = 0 ; i < HOST_NUM ; i++) {
            counters.put(String.valueOf(i), new AtomicInteger(0));
            hosts.add(String.valueOf(i));
        }

        final LoadBalancedHostManager manager = new LoadBalancedHostManager();
        manager.setHostList(hosts);

        for (int i = 0 ; i < TASK_NUM ; i++) {
            newTask(counters, manager);
        }

    }

    private static void newTask(final Map<String, AtomicInteger> counters, final LoadBalancedHostManager manager) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignored) {
                }
                for (int i = 0 ; i < 1000 ; i++){
                    AtomicInteger counter = counters.get(manager.nextHost().getUrl());
                    counter.incrementAndGet();
                }
                StringBuilder stringBuilder = new StringBuilder();
                for (AtomicInteger counter : counters.values()){
                    stringBuilder.append(counter.get());
                    stringBuilder.append(" ");
                }
                System.out.println("counters " + stringBuilder.toString());
            }
        }).start();
    }

}
