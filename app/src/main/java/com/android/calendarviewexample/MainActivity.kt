package com.android.calendarviewexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.children
import com.android.calendarviewexample.databinding.ActivityMainBinding
import com.android.calendarviewexample.databinding.CalendarDayLayoutBinding
import com.android.calendarviewexample.databinding.CalendarHeaderBinding
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding
    private val today = LocalDate.now()
    private var selectedDate: LocalDate = today
    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            val minDateLong =
                LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli()
            val min = Calendar.getInstance()
            min.time = Date(minDateLong)
//            calendarView2.setMinimumDate(min)
//            calendarView.minDate = minDateLong
//            calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
//                Log.d(TAG, "onCreate: Date: ${view.date}")
//                Log.d(TAG, "onCreate: Year: $year")
//                Log.d(TAG, "onCreate: month: $month")
//                Log.d(TAG, "onCreate: dayOfMonth: $dayOfMonth")
//            }
//            btnPrintCurrentDate.setOnClickListener {
//                Log.d(TAG, "onCreate: Date: ${calendarView.date}")
//            }
            //---------------------------
        }
        binding.calendarView.monthScrollListener = {
//            binding.tvTitle.text = if (it.yearMonth.year == today.year) {
//                titleSameYearFormatter.format(it.yearMonth)
//            } else {
//                titleFormatter.format(it.yearMonth)
//            }
            binding.tvTitle.text = titleFormatter.format(it.yearMonth)
            // Select the first day of the visible month.
//            selectDate(it.yearMonth.atDay(1))
        }
        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(3)
        val endMonth = currentMonth.plusMonths(3)
        configureBinders(daysOfWeek)
        binding.calendarView.apply {
            setup(startMonth, endMonth, daysOfWeek.first())
            scrollToMonth(currentMonth)
        }
    }


    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayLayoutBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        selectDate(day.date)
                    }
                }
            }
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.binding.calendarDayText

                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    textView.makeVisible()
                    when (data.date) {
                        selectedDate -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                        }
                        today -> {
                            textView.setTextColorRes(R.color.darkBlue)
                            textView.setBackgroundResource(R.drawable.calendar_today_bg)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.black)
                            textView.background = null
                        }
                    }
                } else {
                    textView.makeInVisible()
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val calendarHeaderLayout = CalendarHeaderBinding.bind(view).calendarHeaderLayout.root
        }
        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.calendarHeaderLayout.tag == null) {
                        container.calendarHeaderLayout.tag = data.yearMonth
                        container.calendarHeaderLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].name.first().toString()
                                tv.setTextColorRes(R.color.black)
                            }
                    }
                }
            }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            //Needed for clearing background from previous selection
            binding.calendarView.notifyDateChanged(oldDate)
            //Needed for adding background for new selection
            binding.calendarView.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    private fun updateAdapterForDate(date: LocalDate) {
        //TODO: update header
        binding.tvSelectedDate.text = selectionFormatter.format(date)
    }
}