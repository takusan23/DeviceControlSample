package io.github.takusan23.devicecontrolsample

import android.app.PendingIntent
import android.content.Intent
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.ControlAction
import android.service.controls.actions.FloatAction
import android.service.controls.templates.*
import java.util.concurrent.Flow
import java.util.function.Consumer

/** デバイスコントロールのサービス */
class DeviceControlService : ControlsProviderService() {

    /** 利用可能なデバイスコントロールはここで */
    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> {
        val control = createControl(0f)
        return Flow.Publisher { subscriber ->
            subscriber.onNext(control)
            subscriber.onComplete()
        }
    }

    /** Controlの操作に使う */
    private lateinit var subscriber: Flow.Subscriber<in Control>

    /** 実際にデバイスコントロールを有効にした場合はここ */
    override fun createPublisherFor(p0: MutableList<String>): Flow.Publisher<Control> {
        // ここで値を取得するなりする。今回はハードコート
        val control = createControl(0f)
        return Flow.Publisher<Control> {
            subscriber = it
            it.onSubscribe(object : Flow.Subscription {
                override fun request(p0: Long) {

                }

                override fun cancel() {

                }
            })
            it.onNext(control)
        }
    }

    /** デバイスコントロールを操作したときはここ */
    override fun performControlAction(p0: String, p1: ControlAction, p2: Consumer<Int>) {
        // システムに処理中とおしえる
        p2.accept(ControlAction.RESPONSE_OK)
        if (p1 is FloatAction) {
            val control = createControl(p1.newValue)
            subscriber.onNext(control)
        }
    }

    /**
     * コントロールを返す関数
     * @param floatValue シークを進める場合は入れてください
     * */
    private fun createControl(floatValue: Float = 0f): Control {
        // コントロールを長押ししたときのインテント
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 4545, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return Control.StatefulBuilder("sample_control", pendingIntent).apply {
            setTitle("シーリングライト")
            setSubtitle("リビング")
            setDeviceType(DeviceTypes.TYPE_LIGHT)
            setStatus(Control.STATUS_OK)
            // 値を調整できるように
            setControlTemplate(RangeTemplate("sample_range", 0f, 10f, floatValue, 1f, "%.0f"))
        }.build()
    }

}