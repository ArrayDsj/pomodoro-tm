/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pomodoro.model

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import pomodoro.model.PomodoroState.Type

/**
 * Class for persisting pomodoro state.
 * It was not implemented as part of [Settings] class
 * because instances of this class cannot be directly changed by user.

 * Thread-safe because it's accessed from [ControlThread] and
 * IntelliJ platform thread which persists it.
 */
@State(name = "PomodoroState", storages = arrayOf(Storage(id = "other", file = "\$APP_CONFIG$/pomodoro.state.xml")))
data class PomodoroState(
        var type: Type = Type.STOP,
        var lastState: Type = Type.STOP,
        var startTime: Long = 0,
        var lastUpdateTime: Long = 0,
        var pomodorosAmount: Int = 0,
        var progress: Int = 0,
        var progressMax: Int = 0
) : PersistentStateComponent<PomodoroState> {

    override fun getState() = this

    @Synchronized override fun loadState(persistence: PomodoroState) = XmlSerializerUtil.copyBean(persistence, this)

    enum class Type {
        /** Pomodoro timer was not started or was stopped during pomodoro or break. */
        STOP,
        /** Pomodoro in progress. */
        RUN,
        /** Pomodoro during break. Can only happen after completing a pomodoro. */
        BREAK
    }
}