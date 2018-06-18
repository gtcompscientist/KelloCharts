package co.csadev.kellocharts.model.dsl

import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.util.ChartUtils

@DslMarker
annotation class AxisValueDsl

@AxisValueDsl
fun axisValue(block: AxisValue.() -> Unit): AxisValue = AxisValue().apply(block)
@AxisValueDsl
class AXISVALUES: ArrayList<AxisValue>() {
    fun axis(block: AxisValue.() -> Unit) {
        add(AxisValue().apply(block))
    }
}

@DslMarker
annotation class BubbleValueDsl

@BubbleValueDsl
fun bubbleValue(block: BubbleValueBuilder.() -> Unit): BubbleValue = BubbleValueBuilder().apply(block).build()
@BubbleValueDsl
class BubbleValueBuilder {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var color: Int = ChartUtils.DEFAULT_COLOR
    var label: CharArray? = null
    var shape: ValueShape? = ValueShape.CIRCLE

    fun build() = BubbleValue(x, y, z, color, label, shape)
}
@BubbleValueDsl
class BUBBLEVALUES: ArrayList<BubbleValue>() {
    fun bubble(block: BubbleValue.() -> Unit) {
        add(BubbleValue().apply(block))
    }
}

@DslMarker
annotation class PointValueDsl

@PointValueDsl
fun pointValue(block: PointValueBuilder.() -> Unit): PointValue = PointValueBuilder().apply(block).build()
@PointValueDsl
class PointValueBuilder {
    var x: Float = 0f
    var y: Float = 0f
    var label: CharArray? = null

    fun build() = PointValue(x, y, label)
}
@PointValueDsl
class POINTVALUES: ArrayList<PointValue>() {
    fun point(block: PointValue.() -> Unit) {
        add(PointValue().apply(block))
    }
}

@DslMarker
annotation class SelectedValueDsl

@SelectedValueDsl
fun selectedValue(block: SelectedValue.() -> Unit): SelectedValue = SelectedValue().apply(block)
@SelectedValueDsl
class SELECTEDVALUES: ArrayList<SelectedValue>() {
    fun point(block: SelectedValue.() -> Unit) {
        add(SelectedValue().apply(block))
    }
}

@DslMarker
annotation class SliceValueDsl

@SliceValueDsl
fun sliceValue(block: SliceValue.() -> Unit): SliceValue = SliceValue().apply(block)
@SliceValueDsl
class SLICEVALUES: ArrayList<SliceValue>() {
    fun slice(block: SliceValue.() -> Unit) {
        add(SliceValue().apply(block))
    }
}

@DslMarker
annotation class SubcolumnValueDsl

@SubcolumnValueDsl
fun subcolumnValue(block: SubcolumnValue.() -> Unit): SubcolumnValue = SubcolumnValue().apply(block)
@SubcolumnValueDsl
class SUBCOLUMNVALUES: ArrayList<SubcolumnValue>() {
    fun subcolumn(block: SubcolumnValue.() -> Unit) {
        add(SubcolumnValue().apply(block))
    }
}
