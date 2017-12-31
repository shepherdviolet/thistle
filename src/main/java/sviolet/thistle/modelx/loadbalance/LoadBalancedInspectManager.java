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

import sviolet.thistle.model.thread.LazySingleThreadPool;
import sviolet.thistle.util.common.ThreadPoolExecutorUtils;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * 均衡负载--网络状态探测管理器
 */
public class LoadBalancedInspectManager {

    private LoadBalancedHostManager hostManager;
    private List<LoadBalanceInspector> inspectors;

    private LazySingleThreadPool dispatchThreadPool = new LazySingleThreadPool("LoadBalancedInspectManager-dispatch-%d");
    private Executor inspectThreadPool = ThreadPoolExecutorUtils.newInstance(0, Integer.MAX_VALUE, 60, "LoadBalancedInspectManager-inspect-%d");

    public LoadBalancedInspectManager() {
    }

    public LoadBalancedInspectManager(LoadBalancedHostManager hostManager, List<LoadBalanceInspector> inspectors) {
        this.hostManager = hostManager;
        this.inspectors = inspectors;
    }

    /**
     * [线程不安全]
     * 设置远端管理器
     * @param hostManager 远端管理器
     */
    public void setHostManager(LoadBalancedHostManager hostManager) {
        this.hostManager = hostManager;
    }

    /**
     * [线程不安全]
     * 设置网络状态探测器
     * @param inspectors 探测器
     */
    public void setInspectors(List<LoadBalanceInspector> inspectors) {
        this.inspectors = inspectors;
    }



}
