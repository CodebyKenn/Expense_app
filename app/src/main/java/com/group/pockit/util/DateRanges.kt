package com.group.pockit.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** The Day / Week / Month switch on the Analysis screen. */
enum class Period { DAY, WEEK, MONTH }

/** A time window in epoch millis: from is included, to is excluded. label is ready to show. */
data class DateRange(val from: Long, val to: Long, val label: String)

object DateRanges {

    private val dayFormat = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
    private val monthFormat = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)

    /**
     * The window that contains [anchor] for the chosen period.
     *   DAY   -> "Oct 24"
     *   WEEK  -> Monday to Sunday, "Oct 19 - Oct 25"
     *   MONTH -> "Oct 2026"
     */
    fun of(period: Period, anchor: LocalDate, zone: ZoneId = ZoneId.systemDefault()): DateRange {
        val start: LocalDate
        val endExclusive: LocalDate
        when (period) {
            Period.DAY -> {
                start = anchor
                endExclusive = anchor.plusDays(1)
            }
            Period.WEEK -> {
                start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                endExclusive = start.plusDays(7)
            }
            Period.MONTH -> {
                start = anchor.withDayOfMonth(1)
                endExclusive = start.plusMonths(1)
            }
        }
        val label = when (period) {
            Period.DAY -> start.format(dayFormat)
            Period.WEEK -> "${start.format(dayFormat)} - ${endExclusive.minusDays(1).format(dayFormat)}"
            Period.MONTH -> start.format(monthFormat)
        }
        return DateRange(
            from = start.atStartOfDay(zone).toInstant().toEpochMilli(),
            to = endExclusive.atStartOfDay(zone).toInstant().toEpochMilli(),
            label = label
        )
    }

    /** The "<" and ">" arrows: move the anchor back or forward by whole periods. */
    fun shift(anchor: LocalDate, period: Period, steps: Long): LocalDate = when (period) {
        Period.DAY -> anchor.plusDays(steps)
        Period.WEEK -> anchor.plusWeeks(steps)
        Period.MONTH -> anchor.plusMonths(steps)
    }

    /** Whole calendar month, used by the Budget screen. */
    fun monthRange(month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): DateRange =
        of(Period.MONTH, month.atDay(1), zone)

    /** Date picker + time picker -> the single timestamp stored on a record. */
    fun toMillis(date: LocalDate, time: LocalTime, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atTime(time).atZone(zone).toInstant().toEpochMilli()
}
