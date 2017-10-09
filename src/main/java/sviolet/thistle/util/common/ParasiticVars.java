/*
 * Copyright (C) 2015 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.common;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.thistle.model.thread.LazySingleThreadPool;
import sviolet.thistle.common.entity.Destroyable;

/**
 * <p>寄生变量</p>
 *
 * <p>警告: 慎用寄生变量, 不当的使用可能造成内存泄漏.</p>
 *
 * <p>
 * 生命周期依附宿主的变量, 不建议存放过大的对象.<br/>
 * 每一个变量需声明其宿主(host)和变量名(key), 同一个宿主名下不可存在同名变量. 变量会被强引用, 直到生命周期结束,
 * 变量的生命周期取决于宿主的生命周期, 即宿主被系统GC时, 其名下所有变量生命周期结束, 也可以主动调用remove()方法移除变量.
 * 变量生命周期结束时, 会解除对其的强引用, 若变量实现了{@link Destroyable}接口, 会被调用{@link Destroyable#onDestroy()}
 * 方法用于销毁实例, 对象可能在下一次系统GC时被销毁, 若变量被其他对象强引用, 则不会被GC.
 * </p>
 *
 * <p>*************************************************************</p>
 *
 * <p>
 * host 宿主 : <br/>
 * 不可为空,宿主被系统GC时,其名下所有变量会被释放引用,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
 * </p>
 *
 * <p>
 * key 变量名 : <br/>
 * 不可为空,用于区分相同宿主名下的不同变量,相同宿主名下,若变量名重复,原变量实例会被销毁({@link Destroyable#onDestroy()})
 * </p>
 *
 * <p>
 * param 变量 : <br/>
 * 生命周期依附于宿主的变量,当宿主被系统GC时,变量也会被释放引用,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁.
 * 变量实例可能在下一次系统GC时销毁, 若变量被其他对象强引用, 则不会被GC
 * </p>
 *
 * Created by S.Violet on 2015/11/24.
 */
public class ParasiticVars {

    private static WeakReference<GcHandler> gcHandler;//gc事件监听器

    private static Map<String, HostHolder> hosts;//变量宿主

    private static LazySingleThreadPool gcTaskPool;//gc任务执行线程池

    private final static ReentrantLock lock = new ReentrantLock();

    /**
     * 设置寄生变量
     *
     * @param host 宿主 不为空,宿主被系统GC时,其名下所有变量会被GC,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     * @param key 变量名 不为空,相同宿主名下,若变量名重复,原变量实例会被销毁({@link Destroyable#onDestroy()})
     * @param param 变量 不为空,生命周期依附于宿主
     */
    public static void set(Object host, String key, Object param){

        if (host == null || param == null)
            return;
        if (key == null)
            throw new NullPointerException("[ParasiticVars] key == null");

        init();//初始化

        String hostKey = calculateHostKey(host);//计算宿主Key

        try{
            lock.lock();
            //获取已存在的宿主
            HostHolder hostHolder = hosts.get(hostKey);
            //宿主不存在则加入
            if (hostHolder == null){
                hostHolder = new HostHolder(host);
                hosts.put(hostKey, hostHolder);
            }
            //变量存入宿主
            hostHolder.set(key, param);
        }finally {
            lock.unlock();
        }

    }

    /**
     * 获取寄生变量
     *
     * @param host 宿主 不为空,宿主被系统GC时,其名下所有变量会被GC,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     * @param key 变量名 不为空,相同宿主名下,若变量名重复,原变量实例会被销毁({@link Destroyable#onDestroy()})
     * @return 变量 可为空,生命周期依附于宿主
     */
    public static Object get(Object host, String key){

        if (host == null)
            return null;
        if (key == null)
            throw new NullPointerException("[ParasiticVars] key == null");

        init();//初始化

        String hostKey = calculateHostKey(host);//计算宿主Key

        try{
            lock.lock();
            //获取已存在的宿主
            HostHolder hostHolder = hosts.get(hostKey);
            //宿主存在则获取变量
            if (hostHolder != null){
                return hostHolder.get(key);
            }
        }finally {
            lock.unlock();
        }

        //宿主不存在返回null
        return null;
    }

    /**
     * 移除寄生变量, 实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     *
     * @param host 宿主 不为空,宿主被系统GC时,其名下所有变量会被GC,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     * @param key 变量名 不为空
     */
    public static void remove(Object host, String key){

        if (host == null)
            return;
        if (key == null)
            throw new NullPointerException("[ParasiticVars] key == null");

        init();//初始化

        String hostKey = calculateHostKey(host);//计算宿主Key
        HostHolder gcHostHolder = null;

        try{
            lock.lock();
            //获取已存在的宿主
            gcHostHolder = hosts.get(hostKey);
        }finally {
            lock.unlock();
        }

        //宿主存在则从中移除变量
        if (gcHostHolder != null)
            gcHostHolder.remove(key);

    }

    /**
     * 移除指定宿主名下所有寄生变量, 实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     *
     * @param host 宿主 不为空,宿主被系统GC时,其名下所有变量会被GC,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     */
    public static void removeAll(Object host){

        if (host == null)
            return;

        init();//初始化

        String hostKey = calculateHostKey(host);//计算宿主Key
        HostHolder gcHostHolder = null;

        try{
            lock.lock();
            //移除已存在的宿主
            gcHostHolder = hosts.remove(hostKey);
        }finally {
            lock.unlock();
        }

        //宿主存在则清空其中的变量
        if (gcHostHolder != null)
            gcHostHolder.removeAll();

    }

    /**
     * 移除所有寄生变量, 实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     */
    public static void removeAll(){

        Map<String, HostHolder> gcHosts = null;

        try{
            lock.lock();
            gcHosts = hosts;
            hosts = null;
        }finally {
            lock.unlock();
        }

        if (gcHosts != null) {
            for (Map.Entry<String, HostHolder> entry : gcHosts.entrySet()) {
                entry.getValue().removeAll();
            }
            gcHosts.clear();
        }

    }

    /**
     * 初始化GlobalVars
     */
    private static void init(){
        //宿主Map不存在则新建
        if (hosts == null){
            try{
                lock.lock();
                if (hosts == null){
                    hosts = new HashMap<>();//新建宿主Map
                    gcHandler = new WeakReference<>(new GcHandler());//新建gc事件监听
                }
            }finally {
                lock.unlock();
            }
        }
    }

    private static LazySingleThreadPool getGcTaskPool(){
        if (gcTaskPool == null){
            try{
                lock.lock();
                if (gcTaskPool == null){
                    gcTaskPool = new LazySingleThreadPool();//新建gc任务执行线程池
                }
            }finally {
                lock.unlock();
            }
        }

        return gcTaskPool;
    }

    /**
     * 系统gc时, 清理无宿主变量
     */
    private static void gc(){

        //引用当前值
        final Map<String, HostHolder> hosts = ParasiticVars.hosts;
        if (hosts == null){
            return;
        }

        Map<String, HostHolder> gcHosts = new HashMap<>();//被清理的宿主镜像

        try{
            lock.lock();
            //搜索无宿主的宿主镜像
            for (Map.Entry<String, HostHolder> entry : hosts.entrySet()){
                if (!entry.getValue().isHostExists()){
                    gcHosts.put(entry.getKey(), entry.getValue());
                }
            }
            //清除无宿主的宿主镜像
            for (Map.Entry<String, HostHolder> entry : gcHosts.entrySet()){
                hosts.remove(entry.getKey());
            }
        }finally {
            lock.unlock();
        }

        //清理无宿主的变量
        for (Map.Entry<String, HostHolder> entry : gcHosts.entrySet()){
            entry.getValue().removeAll();
        }

        gcHosts.clear();

        //新建gc事件监听
        gcHandler = new WeakReference<>(new GcHandler());
    }

    /**
     * 计算宿主key
     * @param host 宿主
     */
    private static String calculateHostKey(Object host){
        return host.getClass().getSimpleName() + "#" + host.hashCode();
    }

    /**
     * 系统gc事件监听器
     */
    private static class GcHandler{
        /**
         * 利用WeakPreference引用的对象会被系统gc时清理,
         * 在finalize()方法中执行GlobalVars无宿主变量的清理
         */
        @Override
        protected void finalize() throws Throwable {
            try {
                getGcTaskPool().execute(gcRunnable);
            } finally {
                super.finalize();
            }
        }
    }

    private static Runnable gcRunnable = new Runnable() {
        @Override
        public void run() {
            ParasiticVars.gc();
        }
    };

    /**
     * 宿主镜像<p/>
     *
     * 包含WeakReference引用的宿主本身, 和宿主变量
     */
    private static class HostHolder{

        private WeakReference<Object> host;//宿主
        private Map<String, Object> params = new HashMap<>();//宿主变量
        private final ReentrantLock lock = new ReentrantLock();

        HostHolder(Object host){
            //弱引用宿主
            this.host = new WeakReference<>(host);
        }

        void set(String key, Object param){

            if (key == null || param == null)
                throw new NullPointerException("[ParasiticVars] key == null || param == null");

            Object destroyable;

            try{
                lock.lock();
                //不存在同名变量
                if (!params.containsKey(key)){
                    params.put(key, param);
                    return;
                }
                //存在同名变量
                if(param == params.get(key)){
                    //变量相等则不处理
                    return;
                }else{
                    //变量不同则移除并销毁原有变量
                    destroyable = params.remove(key);
                    params.put(key, param);
                }
            }finally {
                lock.unlock();
            }

            //销毁原变量
            destroy(destroyable);

        }

        Object get(String key){

            if (key == null)
                throw new NullPointerException("[ParasiticVars] key == null");

            try{
                lock.lock();
                return params.get(key);
            }finally {
                lock.unlock();
            }
        }

        void remove(String key){

            if (key == null)
                throw new NullPointerException("[ParasiticVars] key == null");

            Object destroyable;

            try{
                lock.lock();
                destroyable = params.remove(key);
            }finally {
                lock.unlock();
            }

            //销毁原变量
            destroy(destroyable);

        }

        void removeAll(){

            Map<String, Object> destroyables;

            try{
                lock.lock();
                destroyables = params;
                params = new HashMap<>();
            }finally {
                lock.unlock();
            }

            if (destroyables == null)
                return;

            //销毁所有变量
            for (Map.Entry<String, Object> entry : destroyables.entrySet()){
                destroy(entry.getValue());
            }

            destroyables.clear();

        }

        Object getHost(){
            if (host != null)
                return host.get();
            return null;
        }

        /**
         * 宿主是否还存在
         * @return
         */
        boolean isHostExists(){
            return getHost() != null;
        }

        void destroy(Object obj){
            if (obj != null && obj instanceof Destroyable){
                ((Destroyable) obj).onDestroy();
            }
        }

    }

}
