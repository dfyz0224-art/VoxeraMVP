package com.vanoprojects.voxera.email

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.vanoprojects.voxera.ui.screens.QuestionnairePurpose
import com.vanoprojects.voxera.ui.strings.Strings
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val RECIPIENT_EMAIL = "voxera2026@gmail.com"

/**
 * Opens the device email app with To / subject / body prefilled (mailto),
 * without the general Share sheet.
 * @return true if an email app was opened; false if none found.
 */
fun Context.launchQuestionnaireEmail(
    strings: Strings,
    purpose: QuestionnairePurpose,
    purposeLabel: String,
    orgName: String,
    fieldOfActivity: String,
    fieldAndGoal: String,
    contactsFio: String,
    contactsEmail: String,
    contactsPhone: String,
    approxClientsPerDay: String,
    approxPeopleAndFrequency: String,
    approxEmployeesPerDay: String,
    approxEmployeesPerDayMonth: String,
    specialConditions: String
): Boolean {
    val body = buildQuestionnaireEmailBody(
        strings = strings,
        purpose = purpose,
        purposeLabel = purposeLabel,
        orgName = orgName,
        fieldOfActivity = fieldOfActivity,
        fieldAndGoal = fieldAndGoal,
        contactsFio = contactsFio,
        contactsEmail = contactsEmail,
        contactsPhone = contactsPhone,
        approxClientsPerDay = approxClientsPerDay,
        approxPeopleAndFrequency = approxPeopleAndFrequency,
        approxEmployeesPerDay = approxEmployeesPerDay,
        approxEmployeesPerDayMonth = approxEmployeesPerDayMonth,
        specialConditions = specialConditions
    )
    val subject = "${strings.questionnaireEmailSubject} — ${orgName.trim()}"

    // ACTION_SENDTO + mailto: only email clients; not the system Share sheet.
    val mailto = Uri.parse("mailto:$RECIPIENT_EMAIL").buildUpon()
        .appendQueryParameter("subject", subject)
        .appendQueryParameter("body", body)
        .build()
    val intent = Intent(Intent.ACTION_SENDTO, mailto)

    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        // Fallback: rfc822 still targets email apps when possible
        val fallback = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(RECIPIENT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            startActivity(Intent.createChooser(fallback, strings.questionnaireEmailChooserTitle))
            true
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, strings.questionnaireNoEmailApp, Toast.LENGTH_LONG).show()
            false
        }
    }
}

private fun dashIfBlank(s: String): String {
    val t = s.trim()
    return if (t.isEmpty()) "—" else t
}

private fun section(title: String, block: StringBuilder.() -> Unit): String {
    return buildString {
        appendLine()
        appendLine("── $title ──")
        block()
        appendLine()
    }
}

private fun line(label: String, value: String): String =
    "$label: ${dashIfBlank(value)}"

private fun buildQuestionnaireEmailBody(
    strings: Strings,
    purpose: QuestionnairePurpose,
    purposeLabel: String,
    orgName: String,
    fieldOfActivity: String,
    fieldAndGoal: String,
    contactsFio: String,
    contactsEmail: String,
    contactsPhone: String,
    approxClientsPerDay: String,
    approxPeopleAndFrequency: String,
    approxEmployeesPerDay: String,
    approxEmployeesPerDayMonth: String,
    specialConditions: String
): String {
    val now = LocalDateTime.now()
    val dateStr = try {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(now)
    } catch (_: Exception) {
        now.toString()
    }

    return buildString {
        appendLine("╔══════════════════════════════════════════════════════════════╗")
        appendLine("║  ${strings.questionnaireTitle.uppercase()} — Voxera B2B")
        appendLine("╚══════════════════════════════════════════════════════════════╝")
        appendLine()
        appendLine("${strings.questionnaireEmailSentAt}: $dateStr")
        appendLine()

        append(section(strings.purposeOfUse) {
            appendLine(dashIfBlank(purposeLabel))
        })

        append(section(strings.orgName) {
            appendLine(dashIfBlank(orgName))
        })

        when (purpose) {
            QuestionnairePurpose.FINANCIAL, QuestionnairePurpose.HR_SPORTS -> {
                append(section(strings.fieldOfActivity) {
                    appendLine(dashIfBlank(fieldOfActivity))
                })
            }
            QuestionnairePurpose.SAFETY, QuestionnairePurpose.OTHER -> {
                append(section(strings.fieldAndGoal) {
                    appendLine(dashIfBlank(fieldAndGoal))
                })
            }
        }

        append(section(strings.contacts) {
            appendLine(line(strings.contactsFio, contactsFio))
            appendLine(line(strings.contactsEmail, contactsEmail))
            appendLine(line(strings.contactsPhone, contactsPhone))
        })

        when (purpose) {
            QuestionnairePurpose.FINANCIAL -> {
                append(section(strings.approxClientsPerDay) {
                    appendLine(dashIfBlank(approxClientsPerDay))
                })
                append(section(strings.specialConditions) {
                    appendLine(dashIfBlank(specialConditions))
                })
            }
            QuestionnairePurpose.HR_SPORTS -> {
                append(section(strings.approxPeopleAndFrequency) {
                    appendLine(dashIfBlank(approxPeopleAndFrequency))
                })
                append(section(strings.specialConditions) {
                    appendLine(dashIfBlank(specialConditions))
                })
            }
            QuestionnairePurpose.SAFETY -> {
                append(section(strings.approxEmployeesPerDay) {
                    appendLine(dashIfBlank(approxEmployeesPerDay))
                })
            }
            QuestionnairePurpose.OTHER -> {
                append(section(strings.approxEmployeesPerDayMonth) {
                    appendLine(dashIfBlank(approxEmployeesPerDayMonth))
                })
            }
        }

        appendLine("──────────────────────────────────────────────────────────────")
        appendLine(strings.questionnaireEmailFooter)
    }
}
