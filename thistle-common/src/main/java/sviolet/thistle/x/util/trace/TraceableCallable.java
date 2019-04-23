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

package sviolet.thistle.x.util.trace;

import java.util.concurrent.Callable;

/**
 * <p>[异步追踪]Callable</p>
 *
 * <p>1.在实例化时记录追踪信息</p>
 * <p>2.在被调用时使用记录的信息继续追踪</p>
 *
 * @author S.Violet
 */
public abstract class TraceableCallable<T> implements Callable<T> {

    private TraceBaton traceBaton;

    public TraceableCallable() {
        traceBaton = Trace.getBaton();
    }

    @Override
    public T call() throws Exception {
        Trace.handoff(traceBaton);
        return null;
    }

    public abstract T onCall();

}
