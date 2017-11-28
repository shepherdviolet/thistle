/*
 * Copyright (C) 2015-2016 S.Violet
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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Ballot Box</p>
 *
 * Created by S.Violet on 2016/5/10.
 */
public class BallotBox {

    private AtomicInteger count;

    public BallotBox(){
        this(0);
    }

    public BallotBox(int initialValue){
        count = new AtomicInteger(initialValue);
    }

    public void bind(BallotTicket ballotTicket){
        if (ballotTicket != null){
            ballotTicket.bind(this);
        }
    }

    public int getVotesCount(){
        return count.get();
    }

    public boolean isEmpty(){
        return count.get() == 0;
    }

    void increase(){
        count.incrementAndGet();
    }

    void decrease(){
        count.decrementAndGet();
    }

}
