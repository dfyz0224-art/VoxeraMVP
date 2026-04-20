package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.email.launchQuestionnaireEmail
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

enum class QuestionnairePurpose {
  FINANCIAL,
  HR_SPORTS,
  SAFETY,
  OTHER
}

@Composable
fun ForBusinessQuestionnaireScreen(
  onSubmit: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val context = LocalContext.current

  var selectedPurpose by remember { mutableStateOf<QuestionnairePurpose?>(null) }
  var orgName by remember { mutableStateOf("") }
  var fieldOfActivity by remember { mutableStateOf("") }
  var fieldAndGoal by remember { mutableStateOf("") }
  var contactsFio by remember { mutableStateOf("") }
  var contactsEmail by remember { mutableStateOf("") }
  var contactsPhone by remember { mutableStateOf("") }
  var approxClientsPerDay by remember { mutableStateOf("") }
  var approxPeopleAndFrequency by remember { mutableStateOf("") }
  var approxEmployeesPerDay by remember { mutableStateOf("") }
  var approxEmployeesPerDayMonth by remember { mutableStateOf("") }
  var specialConditions by remember { mutableStateOf("") }

  val purposeOptions = listOf(
    QuestionnairePurpose.FINANCIAL to strings.purposeFinancial,
    QuestionnairePurpose.HR_SPORTS to strings.purposeHrSports,
    QuestionnairePurpose.SAFETY to strings.purposeSafety,
    QuestionnairePurpose.OTHER to strings.purposeOther
  )

  Box(modifier = Modifier.fillMaxSize()) {
    if (theme.type == ThemeType.LIGHT) {
      Image(
        painter = painterResource(R.drawable.bg_light),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      VoxeraBackground {}
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      Spacer(modifier = Modifier.height(20.dp))

      ThemedCard(
        modifier = Modifier
          .fillMaxWidth()
          .then(
            if (selectedPurpose == null) Modifier.height(340.dp)
            else Modifier.height(915.dp)
          ),
        gradientIndex = 4,
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 26.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
        ) {
          Text(
            text = strings.purposeOfUse,
            style = cardSectionTitleOnCardStyle(),
            color = colors.textPrimary
          )
          Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))
          purposeOptions.forEach { (purpose, label) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { selectedPurpose = purpose },
              verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
              androidx.compose.material3.RadioButton(
                selected = selectedPurpose == purpose,
                onClick = { selectedPurpose = purpose },
                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                  selectedColor = colors.primaryGlow
                )
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = label,
                style = cardParagraphTextStyle(),
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
              )
            }
          }

          selectedPurpose?.let { purpose ->
            Spacer(modifier = Modifier.height(CardTextSpacing.BetweenSections))

            QuestionnaireTextField(
              label = strings.orgName,
              value = orgName,
              onValueChange = { orgName = it },
              colors = colors
            )

            when (purpose) {
              QuestionnairePurpose.FINANCIAL, QuestionnairePurpose.HR_SPORTS -> {
                QuestionnaireTextField(
                  label = strings.fieldOfActivity,
                  value = fieldOfActivity,
                  onValueChange = { fieldOfActivity = it },
                  colors = colors
                )
              }
              QuestionnairePurpose.SAFETY, QuestionnairePurpose.OTHER -> {
                QuestionnaireTextField(
                  label = strings.fieldAndGoal,
                  value = fieldAndGoal,
                  onValueChange = { fieldAndGoal = it },
                  colors = colors
                )
              }
            }

            Spacer(modifier = Modifier.height(CardTextSpacing.BetweenSections))
            Text(
              text = strings.contacts,
              style = cardSectionTitleOnCardStyle(),
              color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))
            QuestionnaireTextField(
              label = strings.contactsFio,
              value = contactsFio,
              onValueChange = { contactsFio = it },
              colors = colors
            )
            QuestionnaireTextField(
              label = strings.contactsEmail,
              value = contactsEmail,
              onValueChange = { contactsEmail = it },
              colors = colors
            )
            QuestionnaireTextField(
              label = strings.contactsPhone,
              value = contactsPhone,
              onValueChange = { contactsPhone = it },
              colors = colors
            )

            when (purpose) {
              QuestionnairePurpose.FINANCIAL -> {
                QuestionnaireTextField(
                  label = strings.approxClientsPerDay,
                  value = approxClientsPerDay,
                  onValueChange = { approxClientsPerDay = it },
                  colors = colors
                )
                QuestionnaireTextField(
                  label = strings.specialConditions,
                  value = specialConditions,
                  onValueChange = { specialConditions = it },
                  colors = colors,
                  minLines = 3
                )
              }
              QuestionnairePurpose.HR_SPORTS -> {
                QuestionnaireTextField(
                  label = strings.approxPeopleAndFrequency,
                  value = approxPeopleAndFrequency,
                  onValueChange = { approxPeopleAndFrequency = it },
                  colors = colors
                )
                QuestionnaireTextField(
                  label = strings.specialConditions,
                  value = specialConditions,
                  onValueChange = { specialConditions = it },
                  colors = colors,
                  minLines = 3
                )
              }
              QuestionnairePurpose.SAFETY -> {
                QuestionnaireTextField(
                  label = strings.approxEmployeesPerDay,
                  value = approxEmployeesPerDay,
                  onValueChange = { approxEmployeesPerDay = it },
                  colors = colors
                )
              }
              QuestionnairePurpose.OTHER -> {
                QuestionnaireTextField(
                  label = strings.approxEmployeesPerDayMonth,
                  value = approxEmployeesPerDayMonth,
                  onValueChange = { approxEmployeesPerDayMonth = it },
                  colors = colors
                )
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
      ThemedFilledButton(
        text = strings.submitQuestionnaire,
        onClick = {
          val purpose = selectedPurpose
          if (purpose == null) {
            Toast.makeText(context, strings.questionnaireIncomplete, Toast.LENGTH_LONG).show()
            return@ThemedFilledButton
          }
          if (orgName.isBlank() || contactsFio.isBlank() || contactsEmail.isBlank()) {
            Toast.makeText(context, strings.questionnaireIncomplete, Toast.LENGTH_LONG).show()
            return@ThemedFilledButton
          }
          val purposeLabel = purposeOptions.first { it.first == purpose }.second
          val sent = context.launchQuestionnaireEmail(
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
          if (sent) onSubmit()
        },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun QuestionnaireTextField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  colors: ThemeColors,
  minLines: Int = 1
) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = colors.textSecondary
    )
    Spacer(modifier = Modifier.height(4.dp))
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier
        .fillMaxWidth()
        .background(
          colors.buttonBorder.copy(alpha = 0.2f),
          RoundedCornerShape(12.dp)
        )
        .padding(12.dp),
      textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
      cursorBrush = SolidColor(colors.textPrimary),
      minLines = minLines
    )
  }
}
