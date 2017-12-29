/*
 * Copyright (C) 2015-2017 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.modelx.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LoadBalancedHostManager测试案例
 * 1.配置固定不变
 * 2.存在网路故障
 */
public class HostManagerSettingTest {

    private static final int MAX_HOST_NUM = 8;

    public static void main(String[] args) {

        final LoadBalancedHostManager manager = new LoadBalancedHostManager();

        final AtomicInteger noHostCounter = new AtomicInteger(0);
        final AtomicInteger[] counters = new AtomicInteger[MAX_HOST_NUM];
        for (int i = 0 ; i < MAX_HOST_NUM ; i++){
            counters[i] = new AtomicInteger(0);
        }

        setting(manager, new boolean[]{
                false, false, false, false, false, false, false, false
        });

        print(noHostCounter, counters);

        changeSettingInterval(manager);//低频度变配置

        newTask(noHostCounter, counters, manager, 256);

    }

    private static void changeSettingInterval(final LoadBalancedHostManager manager) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, new boolean[]{
                        true, false, false, false, false, false, false, false
                });

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, new boolean[]{
                        false, true, false, false, false, false, false, false
                });

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, new boolean[]{
                        false, false, true, false, false, false, false, false
                });

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, new boolean[]{
                        false, false, false, true, false, false, false, false
                });

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, new boolean[]{
                        true, true, true, true, false, false, false, false
                });

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, new boolean[]{
                        false, false, false, false, true, true, true, true
                });

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, new boolean[]{
                        true, true, true, true, true, true, true, true
                });

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, new boolean[]{
                        true, false, true, false, true, false, true, false
                });

            }
        }).start();
    }

    private static void newTask(final AtomicInteger noHostCounter, final AtomicInteger[] counters, final LoadBalancedHostManager manager, int num) {
        for (int i = 0 ; i < num ; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 6000; i++) {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException ignored) {
                        }
                        LoadBalancedHostManager.Host host = manager.nextHost();
                        if (host == null){
                            noHostCounter.incrementAndGet();
                        } else {
                            AtomicInteger counter = counters[Integer.parseInt(host.getUrl())];
                            counter.incrementAndGet();
                        }
                    }
                }
            }).start();
        }
    }

    private static void setting(LoadBalancedHostManager manager, boolean[] hostSwitchers) {
        StringBuilder stringBuilder = new StringBuilder("switchers ");
        List<String> hosts = new ArrayList<>(0);
        for (int i = 0 ; i < MAX_HOST_NUM ; i++){
            if (hostSwitchers[i]){
                hosts.add(String.valueOf(i));
            }
            stringBuilder.append(hostSwitchers[i]);
            stringBuilder.append(" ");
        }
        manager.setHostList(hosts);
        System.out.println(stringBuilder.toString());
    }

    private static void print(final AtomicInteger noHostCounter, final AtomicInteger[] counters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0 ; i < 60 ; i++) {
                    for (int j = 0; j < 20; j++) {
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException ignored) {
                        }

                        StringBuilder stringBuilder = new StringBuilder();
                        for (AtomicInteger counter : counters) {
                            stringBuilder.append(counter.get());
                            stringBuilder.append(" ");
                        }
                        stringBuilder.append("X");
                        stringBuilder.append(noHostCounter.get());
                        System.out.println("counters " + stringBuilder.toString());
                    }
                }
            }
        }).start();
    }

}
