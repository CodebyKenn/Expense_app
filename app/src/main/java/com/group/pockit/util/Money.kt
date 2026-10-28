package com.group.pockit.util

import com.group.pockit.data.model.RecordType
import java.util.Locale

/** Formats money the way the design shows it. */
object Money {

    /** 10500.0 -> "Ksh 10,500"   812500.72 -> "Ksh 812,500.72" */
    fun ksh(amount: Double): String {
        val rounded = Math.round(amount * 100) / 100.0
        val text = if (rounded % 1.0 == 0.0) {
            String.format(Locale.US, "%,.0f", rounded)
        } else {
            String.format(Locale.US, "%,.2f", rounded)
        }
        return "Ksh $text"
    }

    /** Income "+ Ksh 5,500", expense "- Ksh 4,800", transfer "Ksh 2,000". */
    fun signed(type: RecordType, amount: Double): String = when (type) {
        RecordType.INCOME -> "+ ${ksh(amount)}"
        RecordType.EXPENSE -> "- ${ksh(amount)}"
        RecordType.TRANSFER -> ksh(amount)
    }
}
