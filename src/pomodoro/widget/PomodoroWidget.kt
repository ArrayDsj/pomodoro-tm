package pomodoro.widget

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.ui.UIUtil
import pomodoro.PomodoroComponent
import pomodoro.UIBundle
import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroState
import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.Settings
import pomodoro.model.time.Time
import pomodoro.toolkitwindow.ToolwindowPresenter
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ImageIcon
import javax.swing.JComponent


class PomodoroWidget : CustomStatusBarWidget, StatusBarWidget.Multiframe, Settings.ChangeListener {
    private val panelWithIcon = TextPanelWithIcon()
    private lateinit var statusBar: StatusBar
    private val model: PomodoroModel

    init {
        val pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent::class.java)!!
        model = pomodoroComponent.model

        val settings = Settings.instance
        updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget)

        model.addListener(this, object : PomodoroModel.Listener {
            override fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean) {
                ApplicationManager.getApplication().invokeLater { updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget) }
            }
        })
        panelWithIcon.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) { model.onUserSwitchToNextState(Time.now()) }
            override fun mouseEntered(e: MouseEvent?) { statusBar.info = tooltipText(model) }
            override fun mouseExited(e: MouseEvent?) { statusBar.info = "" }
        })
    }

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun getComponent(): JComponent = panelWithIcon

    override fun copy() = PomodoroWidget()

    private fun tooltipText(model: PomodoroModel): String {
        val nextAction = nextActionName(model)
        val pomodorosAmount = model.state.pomodorosAmount
        return UIBundle.message("statuspanel.tooltip", nextAction, pomodorosAmount)
    }

    private fun nextActionName(model: PomodoroModel) = when (model.state.mode) {
        RUN -> UIBundle.message("statuspanel.stop")
        BREAK -> UIBundle.message("statuspanel.stop_break")
        STOP -> UIBundle.message("statuspanel.start")
    }

    private fun updateWidgetPanel(model: PomodoroModel, panelWithIcon: TextPanelWithIcon, showTimeInToolbarWidget: Boolean) {
        panelWithIcon.text = if (showTimeInToolbarWidget) ToolwindowPresenter.formatDuration(model.timeLeft) else ""
        panelWithIcon.icon = model.state.icon()
        panelWithIcon.repaint()
    }

    private fun PomodoroState.icon(): ImageIcon {
        val underDarcula = UIUtil.isUnderDarcula()
        return when (mode) {
            RUN -> if (underDarcula) pomodoroDarculaIcon else pomodoroIcon
            BREAK -> if (underDarcula) pomodoroBreakDarculaIcon else pomodoroBreakIcon
            STOP -> if (underDarcula) pomodoroStoppedDarculaIcon else pomodoroStoppedIcon
        }
    }

    override fun getPresentation(type: StatusBarWidget.PlatformType) = null

    override fun dispose() {
        model.removeListener(this)
    }

    override fun ID() = "Pomodoro"

    override fun onChange(newSettings: Settings) {
        updateWidgetPanel(model, panelWithIcon, newSettings.isShowTimeInToolbarWidget)
    }

    companion object {
        private val pomodoroIcon = loadIcon("/resources/pomodoro.png")
        private val pomodoroStoppedIcon = loadIcon("/resources/pomodoroStopped.png")
        private val pomodoroBreakIcon = loadIcon("/resources/pomodoroBreak.png")
        private val pomodoroDarculaIcon = loadIcon("/resources/pomodoro-inverted.png")
        private val pomodoroStoppedDarculaIcon = loadIcon("/resources/pomodoroStopped-inverted.png")
        private val pomodoroBreakDarculaIcon = loadIcon("/resources/pomodoroBreak-inverted.png")

        private fun loadIcon(filePath: String) = ImageIcon(PomodoroWidget::class.java.getResource(filePath))
    }
}
