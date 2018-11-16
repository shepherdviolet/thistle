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

package sviolet.thistle.model.math;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Ballot Ticket</p>
 *
 * @author S.Violet
 */
public class BallotTicket {

    private AtomicReference<BallotBox> ballotBox;
    private AtomicBoolean value;

    public BallotTicket(boolean initialValue){
        this.ballotBox = new AtomicReference<>(null);
        this.value = new AtomicBoolean(initialValue);
    }

    public void bind(BallotBox ballotBox){
        if (ballotBox == null){
            return;
        }
        if (this.ballotBox.compareAndSet(null, ballotBox)) {
            if (value.get()) {
                ballotBox.increase();
            }
        }
    }

    public boolean getValue(){
        return value.get();
    }

    public void pro(){
        if (value.compareAndSet(false, true)){
            BallotBox ballotBox = this.ballotBox.get();
            if (ballotBox != null) {
                ballotBox.increase();
            }
        }
    }

    public void con(){
        if (value.compareAndSet(true, false)){
            BallotBox ballotBox = this.ballotBox.get();
            if (ballotBox != null) {
                ballotBox.decrease();
            }
        }
    }

}
