# KelloCharts for Android
Kotlin Charts/graphs library for Android compatible with API 21+ (Android 5.0), several chart types with support for scaling, scrolling and animations
Works best when hardware acceleration is available, so API 14+(Android 4.0) is recommended.
Apache License 2.0.

# Build badges and gradle availability coming soon

## Features

 - Line chart(cubic lines, filled lines, scattered points)
 - Column chart(grouped, stacked, negative values)
 - Pie chart
 - Bubble chart
 - Combo chart(columns/lines)
 - Preview charts(for column chart and line chart)
 - Zoom(pinch to zoom, double tap zoom), scroll and fling
 - Custom and auto-generated axes(top, bottom, left, right, inside)
 - Animations

## Screens and Demos

 - Code of a demo application is in `kellocharts-sample` directory 
 - Demo App coming soon

## Usage

Every chart view can be defined in layout xml file:

 ```xml
    <co.csadev.kellocharts.view.LineChartView
        android:id="@+id/chart"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
 ```

 or created in code and added to layout later:

 ```Kotlin
    LineChartView chart = new LineChartView(context)
    layout.addView(chart)
 ```

 Use methods from *Chart classes to define chart behaviour, example methods:

 ```Kotlin
    Chart.isInteractive = isInteractive
    Chart.zoomType = zoomType
    Chart.setContainerScrollEnabled(boolean isEnabled, ContainerScrollType type)
 ```

 Use methods from data models to define how chart looks like, example methods:

 ```Kotlin
    ChartData.axisXBottom = axisX
    ColumnChartData.isStacked = isStacked
    Line.strokeWidth = strokeWidthDp
 ```

 Every chart has its own method to set chart data and its own data model, example for line chart:

 ```Kotlin
    arrayListOf(PointValue(0, 2), PointValue(1, 4), PointValue(2, 3), PointValue(3, 4))

    val line = Line(values, color = Color.BLUE, isCubic = true)
    val lines = arrayListOf(line)

    val data = LineChartData(lines)

    val chart = LineChartView(context)
    chart.lineChartData = data
 ```

 Also, available as a DSL, same example as above:

 ```Kotlin
lineData {
    lines {
        line {
            color = Color.BLUE
            isCubic = true
            pointValues {
                point {
                    x = 0f
                    y = 2f
                }
                point {
                    x = 1f
                    y = 4f
                }
                point {
                    x = 2f
                    y = 3f
                }
                point {
                    x = 3f
                    y = 4f
                }
            }
        }
    }
 }
 ```

 After the chart data has been set you can still modify its attributes but right after that you should call
 `*.*ChartData` setter again to let chart recalculate and redraw data. There is also an option to use copy constructor for deep copy of
 chart data. You can safely modify copy in other threads and pass it to `*.*ChartData` setter later.


## Contributing

Yes :) If you found a bug, have an idea how to improve library or have a question, please create new issue or comment existing one. If you would like to contribute code fork the repository and send a pull request.

## License

	KelloCharts
    Copyright 2018 Charles Anderson

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

---
     KelloCharts library is developed from the original HelloCharts library available:
       https://github.com/lecho/hellocharts-android

---
     KelloCharts library uses code from InteractiveChart sample available 
     on Android Developers page:
	 
       http://developer.android.com/training/gestures/scale.html
