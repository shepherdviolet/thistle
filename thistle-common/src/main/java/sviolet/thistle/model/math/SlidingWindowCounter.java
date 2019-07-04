/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.thistle.model.math;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>时间滑动窗口计数器</p>
 *
 * <p>计数, 并统计一定时间内的计数值</p>
 *
 * <p>例如, 我们要统计最近5分钟内的交易量, 统计精度为10秒, 即samplingDuration=10000, statisticalPeriod=30,
 * (5分钟 = 10秒 * 30, 实际上程序生成了30个计数器, 每个计数器用来累加10秒钟的计数, 这样就能统计最近十分钟的计数值了).
 * 在交易发生时, 调用getAndAdd(1)/addAndGet(1)方法计数. 在需要获取计数值时, 调用get(?)获取最近N个采样周期内的计数值,
 * get(1)获取最近10秒钟内的计数值, get(6)获取最近一分钟内的计数值, getTotal()获取最近五分钟内的计数值. </p>
 *
 * <p>
 *     建议: <br>
 *     1.statisticalPeriod不要设置的太大, 这个值有多少, 内部就有多少个计数器 <br>
 *     2.如果只需要统计一种时间尺度, 比如最近10秒, 建议: new SlidingWindowCounter(10000, 4), get(1) <br>
 *     3.如果需要统计多种时间尺度, 比如最近10秒和最近5分钟, 建议: new SlidingWindowCounter(10000, 30), get(1), getTotal() <br>
 * </p>
 *
 * @author S.Violet
 */
public class SlidingWindowCounter {

    private static final long RESET_THRESHOLD = -3;

    //采样时长, ms, 示例10000
    private final int samplingDuration;
    //统计周期, 统计时长 = 采样时长 * 统计周期, 示例30
    private final int statisticalPeriod;

    //计数器
    private final AtomicIntegerArray counters;

    //计数滑动锁
    private final ReentrantLock lock = new ReentrantLock();

    private volatile int currentCounterIndex = 0;
    private volatile long lastGeneration = Long.MIN_VALUE;

    /**
     * @param samplingDuration 采样时长, ms, 取值范围 > 0
     * @param statisticalPeriod 统计周期, 统计时长 = 采样时长 * 统计周期, 取值范围 [4, 1024]
     */
    public SlidingWindowCounter(int samplingDuration, int statisticalPeriod) {
        if (samplingDuration <= 0) {
            throw new IllegalArgumentException("samplingDuration must > 0");
        }
        if (statisticalPeriod < 4 || statisticalPeriod > 1024) {
            throw new IllegalArgumentException("statisticalPeriod must >= 4 and <= 1024");
        }
        this.samplingDuration = samplingDuration;
        this.statisticalPeriod = statisticalPeriod;
        this.counters = new AtomicIntegerArray(statisticalPeriod);
    }

    /**
     * 先取值后做加法
     * @param delta 增加的数字
     * @return 计算前的数值(当前采样时长内的计数值, 不是整个统计周期内的计数值)
     */
    public int getAndAdd(int delta){
        return getAndAdd(delta, System.currentTimeMillis());
    }

    /**
     * 先做加法后取值
     * @param delta 增加的数字
     * @return 计算后的数值(当前采样时长内的计数值, 不是整个统计周期内的计数值)
     */
    public int addAndGet(int delta) {
        return addAndGet(delta, System.currentTimeMillis());
    }

    /**
     * 先取值后做加法
     * @param delta 增加的数字
     * @param currentTimeMillis 当前时间(毫秒数), 注意, 这个时间不可以回拨, 回拨超过2个单位时间时会触发所有计数清零
     * @return 计算前的数值(当前采样时长内的计数值, 不是整个统计周期内的计数值)
     */
    protected int getAndAdd(int delta, long currentTimeMillis){
        return counters.getAndAdd(getCurrentCounterIndex(currentTimeMillis), delta);
    }

    /**
     * 先做加法后取值
     * @param delta 增加的数字
     * @param currentTimeMillis 当前时间(毫秒数), 注意, 这个时间不可以回拨, 回拨超过2个单位时间时会触发所有计数清零
     * @return 计算后的数值(当前采样时长内的计数值, 不是整个统计周期内的计数值)
     */
    protected int addAndGet(int delta, long currentTimeMillis) {
        return counters.addAndGet(getCurrentCounterIndex(currentTimeMillis), delta);
    }

    /**
     * 获取最近N个采样周期内的计数值
     * @param period 采样周期
     * @return 最近N个采样周期内的计数值
     */
    public int get(int period) {
        return get(period, System.currentTimeMillis());
    }

    /**
     * 获取整个统计周期内的计数值
     * @return 整个统计周期内的计数值
     */
    public int getTotal(){
        return get(statisticalPeriod);
    }

    /**
     * 获取最近N个采样周期内的计数值
     * @param period 采样周期
     * @param currentTimeMillis 当前时间(毫秒数), 注意, 这个时间不可以回拨, 回拨超过2个单位时间时会触发所有计数清零
     * @return 最近N个采样周期内的计数值
     */
    protected int get(int period, long currentTimeMillis) {
        if (period <= 0) {
            return 0;
        } else if (period > statisticalPeriod) {
            period = statisticalPeriod;
        }
        int currentIndex = getCurrentCounterIndex(currentTimeMillis);
        int result = 0;
        for (int i = 0 ; i < period ; i++) {
            result += counters.get(modStatisticalPeriod(currentIndex - i));
        }
        return result;
    }

    private int getCurrentCounterIndex(long currentTimeMillis){
        if (currentTimeMillis < 0) {
            currentTimeMillis = 0;
        }
        int counterIndex = currentCounterIndex;
        long currentGeneration = currentTimeMillis / samplingDuration;
        long elapse = currentGeneration - lastGeneration;
        if (elapse > RESET_THRESHOLD && elapse <= 0) {
            return counterIndex;
        } else {
            try {
                lock.lock();
                elapse = currentGeneration - lastGeneration;
                if (elapse > 0) {
                    slide(elapse);
                } else if (elapse <= RESET_THRESHOLD) {
                    resetAll();
                }
                lastGeneration = currentGeneration;
                return currentCounterIndex;
            } finally {
                lock.unlock();
            }
        }
    }

    private void slide(long elapse){
        //如果间隔时长太长, 就全部重置
        if (elapse >= statisticalPeriod) {
            resetAll();
            return;
        }
        //将滑动部分的计数器清零
        for (int i = currentCounterIndex + 1; i <= currentCounterIndex + elapse ; i++) {
            counters.set(modStatisticalPeriod(i), 0);
        }
        currentCounterIndex = modStatisticalPeriod(currentCounterIndex + elapse);
    }

    private void resetAll(){
        //全部重置
        for (int i = 0; i <= statisticalPeriod; i++) {
            counters.set(modStatisticalPeriod(currentCounterIndex + i), 0);
        }
    }

    private int modStatisticalPeriod(int value){
        value = value % statisticalPeriod;
        if (value < 0) {
            value += statisticalPeriod;
        }
        return value;
    }

    private int modStatisticalPeriod(long value){
        value = value % statisticalPeriod;
        if (value < 0) {
            value += statisticalPeriod;
        }
        return (int) value;
    }

}
