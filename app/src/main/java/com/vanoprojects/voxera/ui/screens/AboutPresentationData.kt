package com.vanoprojects.voxera.ui.screens

import com.vanoprojects.voxera.ui.strings.AppLanguage
import com.vanoprojects.voxera.ui.strings.Strings

sealed interface AboutBlock {
  data class Paragraph(val text: String) : AboutBlock
  data class Bullets(val lines: List<String>) : AboutBlock
}

data class AboutShortSection(
  val title: String,
  val blocks: List<AboutBlock>
)

data class AboutSlide(
  val title: String,
  val body: String
)

fun stringsToAppLanguage(strings: Strings): AppLanguage = when (strings) {
  Strings.Ru -> AppLanguage.RU
  Strings.En -> AppLanguage.EN
  Strings.Zh -> AppLanguage.ZH
  Strings.Kz -> AppLanguage.KZ
  else -> AppLanguage.EN
}

fun aboutShortSections(language: AppLanguage): List<AboutShortSection> = when (language) {
  AppLanguage.RU -> aboutShortRu()
  AppLanguage.EN -> aboutShortEn()
  AppLanguage.ZH -> aboutShortZh()
  AppLanguage.KZ -> aboutShortKz()
}

fun aboutPresentationSlides(language: AppLanguage): List<AboutSlide> = when (language) {
  AppLanguage.RU -> slidesRu()
  AppLanguage.EN -> slidesEn()
  AppLanguage.ZH -> slidesZh()
  AppLanguage.KZ -> slidesKz()
}

// --- Краткое описание ---

private fun aboutShortRu() = listOf(
  AboutShortSection(
    title = "Позиционирование",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Voxera — платформа анализа личности по голосу: многомерные вокальные параметры сопоставляются с психотипной моделью и формируют интерпретируемый профиль."
      ),
      AboutBlock.Paragraph(
        "Классические опросные методики часто длинны и ресурсоёмки. Подход Voxera ориентирован на быстрый, технологичный и персонализированный результат без многочасовых тестов."
      )
    )
  ),
  AboutShortSection(
    title = "Методология",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Ядро — набор шкал голосового анализа (16 с перспективой расширения до 46): каждая шкала реализована отдельным алгоритмом и измеряет конкретный акустический признак."
      ),
      AboutBlock.Paragraph("В отчёте вы получаете:"),
      AboutBlock.Bullets(
        listOf(
          "— сводку по ключевым шкалам и их сочетанию;",
          "— привязку к психотипной классификации;",
          "— понятную интерпретацию без лишней терминологии."
        )
      )
    )
  ),
  AboutShortSection(
    title = "Научная и эмпирическая база",
    blocks = listOf(
      AboutBlock.Paragraph(
        "В основе — международные исследования связи голоса с личностными характеристиками (в т.ч. работы уровня UT Austin 2021, iMotions 2021, Mohammadi & Juslin 2010–2011)."
      ),
      AboutBlock.Paragraph(
        "Собственные исследования Voxera: более 3000 участников; достигаемая точность по психотипам в зависимости от профиля — порядка 70–90%."
      )
    )
  ),
  AboutShortSection(
    title = "Ограничение ответственности",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Результаты носят аналитический характер и не являются медицинским диагнозом, клиническим заключением или основанием для самолечения."
      )
    )
  )
)

private fun aboutShortEn() = listOf(
  AboutShortSection(
    title = "Positioning",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Voxera is a voice-based personality intelligence platform: multidimensional vocal parameters are mapped to a psychotype model and delivered as a structured profile."
      ),
      AboutBlock.Paragraph(
        "Traditional questionnaire methods are often lengthy and costly. Voxera is designed for fast, technology-driven, personalized insight without multi-hour batteries."
      )
    )
  ),
  AboutShortSection(
    title = "Methodology",
    blocks = listOf(
      AboutBlock.Paragraph(
        "The core is a set of voice-analysis scales (16 today, planned expansion to 46): each scale is a dedicated algorithm measuring a specific acoustic signature."
      ),
      AboutBlock.Paragraph("Your report includes:"),
      AboutBlock.Bullets(
        listOf(
          "— a summary of key scales and their combination;",
          "— alignment with a psychotype classification;",
          "— clear interpretation without unnecessary jargon."
        )
      )
    )
  ),
  AboutShortSection(
    title = "Evidence base",
    blocks = listOf(
      AboutBlock.Paragraph(
        "The approach is grounded in peer-reviewed research linking voice to personality traits (e.g. University of Texas at Austin, 2021; iMotions, 2021; Mohammadi & Juslin, 2010–2011)."
      ),
      AboutBlock.Paragraph(
        "Voxera’s own studies: 3,000+ participants; psychotype accuracy often in the 70–90% range depending on profile."
      )
    )
  ),
  AboutShortSection(
    title = "Disclaimer",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Outputs are analytical and are not a medical diagnosis, clinical assessment, or substitute for professional care."
      )
    )
  )
)

private fun aboutShortZh() = listOf(
  AboutShortSection(
    title = "产品定位",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Voxera 是基于语音的人格分析平台：将多维声学参数映射到心理类型模型，生成可解读的结构化画像。"
      ),
      AboutBlock.Paragraph(
        "传统问卷式测评往往耗时较长。Voxera 强调快速、可落地的技术化个性化结果，避免冗长测试。"
      )
    )
  ),
  AboutShortSection(
    title = "方法",
    blocks = listOf(
      AboutBlock.Paragraph(
        "核心为语音分析量表体系（16 项，计划扩展至 46 项）：每项量表对应独立算法，测量特定声学特征。"
      ),
      AboutBlock.Paragraph("报告包含："),
      AboutBlock.Bullets(
        listOf(
          "— 关键量表及其组合摘要；",
          "— 与心理类型分类的对应关系；",
          "— 通俗易懂的解读表述。"
        )
      )
    )
  ),
  AboutShortSection(
    title = "研究与验证",
    blocks = listOf(
      AboutBlock.Paragraph(
        "理论基础来自国际同行研究（语音与性格关联：如 UT Austin 2021、iMotions 2021、Mohammadi & Juslin 2010–2011 等）。"
      ),
      AboutBlock.Paragraph(
        "Voxera 实证研究：3000+ 样本；按心理类型划分的准确率常见约 70–90%。"
      )
    )
  ),
  AboutShortSection(
    title = "声明",
    blocks = listOf(
      AboutBlock.Paragraph(
        "输出为分析性结果，不构成医学诊断、临床意见或治疗依据。"
      )
    )
  )
)

private fun aboutShortKz() = listOf(
  AboutShortSection(
    title = "Позициялау",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Voxera — дауыспен тұлғаны талдау платформасы: көпөлшемді акустикалық параметрлер психотип моделімен салыстырылады және түсінікті профиль береді."
      ),
      AboutBlock.Paragraph(
        "Дәстүрлі сауалнамалар ұзақ әрі ресурс жұмсайды. Voxera жылдам, технологиялық және жекелендірілген нәтижеге бағытталған; ұзақ тесттерсіз."
      )
    )
  ),
  AboutShortSection(
    title = "Әдістеме",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Негізгі бөлігі — дауысты талдау шкалалары (16, 46-ға дейін кеңейту жоспарланған): әр шкала жеке алгоритм, бір акустикалық белгіні өлтейді."
      ),
      AboutBlock.Paragraph("Есепте:"),
      AboutBlock.Bullets(
        listOf(
          "— негізгі шкалалар және олардың комбинациясы;",
          "— психотиптік жіктеуге сәйкестік;",
          "— артық терминсіз түсінікті түсіндірме."
        )
      )
    )
  ),
  AboutShortSection(
    title = "Ғылыми негіз",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Халықаралық зерттеулерге сүйенеді (дауыс пен тұлға қасиеттері: UT Austin 2021, iMotions 2021, Mohammadi & Juslin 2010–2011)."
      ),
      AboutBlock.Paragraph(
        "Voxera зерттеулері: 3000+ қатысушы; психотип бойынша дәлдік профильге байланысты шамамен 70–90%."
      )
    )
  ),
  AboutShortSection(
    title = "Жауапкершілік",
    blocks = listOf(
      AboutBlock.Paragraph(
        "Нәтиже талдамалық сипатта; медициналық диагноз немесе клиникалық қорытынды емес."
      )
    )
  )
)

// --- Полное описание (слайды), по тексту презентации; 15 шагов с равномерной нагрузкой ---

private fun slidesRu() = listOf(
  AboutSlide(
    title = "VOXERA: революция в анализе голосовой индивидуальности",
    body = "VOXERA представляет собой передовую комплексную голосовую AI-экосистему, разработанную в Казахстане. Она интегрирует современные технологии анализа голоса, эмоционального состояния и поведенческих паттернов пользователей для создания инновационных решений."
  ),
  AboutSlide(
    title = "Ограничения классических методик",
    body = "Традиционные методы оценки личности застряли в прошлом — они слишком длительны, нетехнологичны и требуют значительных усилий. Люди хотят, чтобы система правильно и быстро их распознавала для немедленного персонализированного подхода, но старые методы этого не позволяют."
  ),
  AboutSlide(
    title = "Научная основа технологии",
    body = "Глобальные исследования\n\nБолее 50 научных исследований было проведено по всему миру, подтверждая связь между вокальными характеристиками и чертами личности человека. Международные исследования показали, что использование голоса для психологического профилирования поддерживается научными патентами и технологиями (iMotions, 2021)."
  ),
  AboutSlide(
    title = "Исследование Техасского университета",
    body = "Исследование Техасского университета (2021) подтвердило, что психологические черты могут быть предсказаны на основе лингвистических и акустических данных голоса."
  ),
  AboutSlide(
    title = "Экспертные данные",
    body = "Эксперты и исследователи, такие как Г. Мохаммади и М. Джаслин, доказали корреляцию между голосом и чертами личности, такими как экстраверсия и эмоциональная стабильность (2010, 2011)."
  ),
  AboutSlide(
    title = "Результаты VOXERA",
    body = "Более 3000 человек приняли участие в исследованиях VOXERA, как лично, так и посредством аудиозаписей. Тесты показывают высокий уровень точности (70–90%) в зависимости от психотипов по голосу, что подтверждается положительными отзывами участников."
  ),
  AboutSlide(
    title = "Многомерный анализ голоса",
    body = "Технология основана на 16 шкалах анализа голоса, которые планируется расширить до 46. Каждая шкала представляет собой алгоритм, мини-программу, измеряющую определённый параметр голоса: плотность, музыкальность, громкость, структуру и другие."
  ),
  AboutSlide(
    title = "Связь с личностью",
    body = "Каждый параметр голоса коррелирует с определённой чертой личности (например, музыкальность с эмоциональной гибкостью)."
  ),
  AboutSlide(
    title = "Уникальный паттерн",
    body = "Комбинация значений по нескольким шкалам формирует уникальный поведенческий паттерн личности."
  ),
  AboutSlide(
    title = "Многомерный куб",
    body = "Отсканированные голоса распределяются в виртуальном многомерном кубе с отмеченными областями для идентификации схожих психотипов.\n\nВ настоящее время пространство разделено на 8 регионов с возможностью дальнейшего расширения. Точность классификации увеличивается за счёт добавления новых шкал и алгоритмов, что потенциально позволяет идентифицировать десятки типов личности и их комбинаций."
  ),
  AboutSlide(
    title = "Комплексный анализ голосовых паттернов",
    body = "Анализаторы используют специальные микросервисы для оценки основных параметров голосов, которые затем соотносятся с «ноотипной матрицей» — системой классификации психотипов."
  ),
  AboutSlide(
    title = "Этапы анализа",
    body = "01 · 46 голосовых анализаторов\nИзмерение множества параметров голоса для создания полного профиля.\n\n02 · Группировка данных\nПостроение классификационной модели личностей на основе 8, 12, 16 или N психотипов.\n\n03 · Корреляция\nСвязь между физиологическим звучанием голоса и психологическими аспектами личности."
  ),
  AboutSlide(
    title = "Ключевые компоненты анализа",
    body = "• Плотность и энергетика голоса\n• Музыкальность и темп голоса\n• Пространственный объём голоса\n• Гармоничность и атмосферность\n• Структура речи\n• Чёткость и вариативность речи\n• Микроколебания и вибрато\n• Конгруэнтность голоса"
  ),
  AboutSlide(
    title = "Преимущества технологии",
    body = "90% — точность анализа. Высокая точность в определении психотипа и прогнозировании поведения.\n\n46 — параметров голоса. Комплексный многомерный анализ характеристик голоса.\n\n3000+ — участников исследования. Обширная база данных для валидации технологии."
  ),
  AboutSlide(
    title = "Дополнительные преимущества",
    body = "Прогнозирование поведения\nСпособность прогнозировать поведенческие паттерны на основе голосовых данных с высокой степенью надёжности.\n\nИнтеграция методологий\nСовместимость с существующими психотипическими методами и классификациями личности.\n\nБыстрый анализ\nМгновенные результаты без длительных тестов и опросников."
  )
)

private fun slidesEn() = listOf(
  AboutSlide(
    title = "VOXERA: a revolution in voice personality analysis",
    body = "VOXERA is an advanced, comprehensive voice AI ecosystem developed in Kazakhstan. It integrates state-of-the-art analysis of voice, emotional state, and user behavioral patterns to deliver innovative solutions."
  ),
  AboutSlide(
    title = "Limits of traditional methods",
    body = "Traditional personality assessment is stuck in the past—it is too long, not technology-driven, and demands significant effort. People expect to be recognized correctly and quickly so they receive a personalized approach at once; legacy methods rarely allow that."
  ),
  AboutSlide(
    title = "Scientific basis of the technology",
    body = "Global research\n\nMore than 50 scientific studies worldwide confirm the link between vocal characteristics and personality traits. International research shows that voice-based psychological profiling is supported by scientific patents and technologies (iMotions, 2021)."
  ),
  AboutSlide(
    title = "University of Texas study",
    body = "A University of Texas study (2021) confirmed that psychological traits can be predicted from linguistic and acoustic voice data."
  ),
  AboutSlide(
    title = "Expert evidence",
    body = "Experts and researchers such as G. Mohammadi and M. Juslin have demonstrated correlations between voice and personality traits such as extraversion and emotional stability (2010, 2011)."
  ),
  AboutSlide(
    title = "VOXERA results",
    body = "More than 3,000 people participated in VOXERA studies, both in person and via audio recordings. Tests show high accuracy (70–90%) depending on psychotypes by voice, supported by positive participant feedback."
  ),
  AboutSlide(
    title = "Multidimensional voice analysis",
    body = "The technology is based on 16 voice-analysis scales, planned to expand to 46. Each scale is an algorithm—a mini-program measuring a specific voice parameter: density, musicality, loudness, structure, and more."
  ),
  AboutSlide(
    title = "Connection to personality",
    body = "Each voice parameter correlates with a specific personality trait (for example, musicality with emotional flexibility)."
  ),
  AboutSlide(
    title = "Unique pattern",
    body = "The combination of values across several scales forms a unique behavioral personality pattern."
  ),
  AboutSlide(
    title = "Multidimensional cube",
    body = "Scanned voices are distributed in a virtual multidimensional cube with regions marked to identify similar psychotypes.\n\nThe space is currently divided into 8 regions with room to grow. Classification accuracy increases as new scales and algorithms are added, potentially identifying dozens of personality types and combinations."
  ),
  AboutSlide(
    title = "Comprehensive analysis of voice patterns",
    body = "Analyzers use dedicated microservices to evaluate core voice parameters, then map them to the “nootype matrix”—a psychotype classification system."
  ),
  AboutSlide(
    title = "Analysis stages",
    body = "01 · 46 voice analyzers\nMeasuring many voice parameters to build a complete profile.\n\n02 · Data grouping\nBuilding a personality classification model on 8, 12, 16, or N psychotypes.\n\n03 · Correlation\nLinking the physiological sound of the voice to psychological aspects of personality."
  ),
  AboutSlide(
    title = "Key components of analysis",
    body = "• Voice density and energy\n• Voice musicality and tempo\n• Spatial volume of the voice\n• Harmoniousness and atmosphere\n• Speech structure\n• Speech clarity and variability\n• Micro-oscillations and vibrato\n• Voice congruence"
  ),
  AboutSlide(
    title = "Technology advantages",
    body = "90% — analysis accuracy. High accuracy in determining psychotype and predicting behavior.\n\n46 — voice parameters. Comprehensive multidimensional analysis of voice characteristics.\n\n3000+ — research participants. An extensive database for technology validation."
  ),
  AboutSlide(
    title = "Further advantages",
    body = "Behavioral prediction\nAbility to predict behavioral patterns from voice data with high reliability.\n\nMethodology integration\nCompatibility with existing psychotypical methods and personality classifications.\n\nRapid analysis\nInstant results without lengthy tests and questionnaires."
  )
)

private fun slidesZh() = listOf(
  AboutSlide(
    title = "VOXERA：语音个体性分析的革新",
    body = "VOXERA 是由哈萨克斯坦研发的先进综合语音 AI 生态，整合前沿的语音分析、情绪状态与用户行为模式技术，以打造创新解决方案。"
  ),
  AboutSlide(
    title = "传统方法的局限",
    body = "传统人格评估仍停留在过去——耗时长、技术化不足、投入大。用户希望被系统正确、快速地识别，并立刻获得个性化体验，而旧方法难以满足。"
  ),
  AboutSlide(
    title = "技术的科学依据",
    body = "全球研究\n\n全球已开展逾 50 项研究，证实声音特征与人格特质相关。国际研究表明，基于语音的心理画像有科学专利与技术支撑（如 iMotions，2021）。"
  ),
  AboutSlide(
    title = "德州大学研究（2021）",
    body = "德州大学（2021）研究证实：可依据语言学及声学语音数据预测心理特质。"
  ),
  AboutSlide(
    title = "专家研究",
    body = "G. Mohammadi 与 M. Juslin 等学者证明：语音与外倾性、情绪稳定性等特质存在相关（2010、2011）。"
  ),
  AboutSlide(
    title = "VOXERA 研究结果",
    body = "逾 3000 人参与 VOXERA 研究（现场与录音）。按语音划分心理类型时，准确度可达 70–90%，获参与者积极反馈。"
  ),
  AboutSlide(
    title = "多维语音分析",
    body = "技术基于 16 项语音分析量表，计划扩展至 46 项。每项量表为独立算法（小型程序），测量密度、音乐性、响度、结构等参数。"
  ),
  AboutSlide(
    title = "与人格的关联",
    body = "每个语音参数与特定人格维度相关（例如音乐性与情绪灵活性）。"
  ),
  AboutSlide(
    title = "独特模式",
    body = "多量表取值的组合形成独特的行为人格模式。"
  ),
  AboutSlide(
    title = "多维立方体",
    body = "扫描后的语音映射到虚拟多维立方体，以区域标识相近心理类型。\n\n当前空间分为 8 个区域，可继续扩展。增加量表与算法可提高分类精度，潜在可识别数十种人格类型及其组合。"
  ),
  AboutSlide(
    title = "语音模式的综合分析",
    body = "分析器通过微服务评估核心语音参数，并与「诺型矩阵」（nootype matrix）——心理类型分类体系——相对应。"
  ),
  AboutSlide(
    title = "分析阶段",
    body = "01 · 46 路语音分析器\n测量多项语音参数以构建完整画像。\n\n02 · 数据分组\n构建基于 8、12、16 或 N 种心理类型的人格分类模型。\n\n03 · 关联\n连接发声的生理特征与人格心理层面。"
  ),
  AboutSlide(
    title = "关键分析组件",
    body = "• 语音密度与能量\n• 音乐性与节奏\n• 空间体积感\n• 和谐与氛围\n• 语篇结构\n• 清晰度与变异性\n• 微振荡与颤音\n• 声音一致性"
  ),
  AboutSlide(
    title = "技术优势",
    body = "90% — 分析准确度。心理类型识别与行为预测精度高。\n\n46 — 语音参数。多维综合刻画。\n\n3000+ — 研究参与者。大规模验证数据。"
  ),
  AboutSlide(
    title = "更多优势",
    body = "行为预测\n基于语音数据以高可靠性预测行为模式。\n\n方法整合\n与既有心理类型方法与分类兼容。\n\n快速分析\n即时结果，无需冗长测试与问卷。"
  )
)

private fun slidesKz() = listOf(
  AboutSlide(
    title = "VOXERA: дауыстық жеке ерекшелік талдауындағы төңкеріс",
    body = "VOXERA — Қазақстанда әзірленген дауыстық AI экожүйесі: дауыс, эмоционалдық күй және мінез-құлық үлгілерін заманауи талдайды және инновациялық шешімдер береді."
  ),
  AboutSlide(
    title = "Дәстүрлі әдістердің шектеулері",
    body = "Дәстүрлі тұлға бағалау әдістері ұзақ, технологиялық емес және көп күш талап етеді. Адамдар жүйеден дұрыс және жылдам тануды, дереу жекелендірілген тәсілді күтеді; ескі әдістер бұған сирек сәйкес келеді."
  ),
  AboutSlide(
    title = "Технологияның ғылыми негізі",
    body = "Жаһандық зерттеулер\n\n50-ден астам ғылыми жұмыс дауыстық сипаттамалар мен тұлға ерекшеліктерін байланыстырады. Халықаралық зерттеулер дауыс негізіндегі психологиялық профильдеуді ғылыми патенттер мен технологиялар қолдайтынын көрсетеді (iMotions, 2021)."
  ),
  AboutSlide(
    title = "Техас университетінің зерттеуі",
    body = "Техас университетінің 2021 жылғы зерттеуі лингвистикалық және акустикалық дауыс деректері бойынша психологиялық сипаттамаларды болжауға болатынын растады."
  ),
  AboutSlide(
    title = "Сарапшылар дерегі",
    body = "Г. Мохаммади және М. Джаслин сияқты сарапшылар дауыс пен экстраверсия, эмоционалдық тұрақтылық сияқты тұлға сипаттамаларының корреляциясын дәлелдеді (2010, 2011)."
  ),
  AboutSlide(
    title = "VOXERA нәтижелері",
    body = "3000-дан астам адам VOXERA зерттеулеріне қатысты — тікелей және аудио арқылы. Тесттер психотип бойынша дауыстан 70–90% дәлдікке дейін көрсетеді; қатысушылар оң пікір береді."
  ),
  AboutSlide(
    title = "Көпөлшемді дауыс талдауы",
    body = "Технология 16 дауысты талдау шкаласына негізделеді, 46-ға дейін кеңейтілуі жоспарланған. Әр шкала — белгілі бір параметрді өлшейтін алгоритм (мини-бағдарлама): тығыздық, музыкалылық, дыбыс, құрылым және т.б."
  ),
  AboutSlide(
    title = "Тұлғаға байланыс",
    body = "Әр дауыстық параметр белгілі бір тұлға сипатымен корреляцияланады (мысалы музыкалылық — эмоционалдық икемділік)."
  ),
  AboutSlide(
    title = "Бірегей үлгі",
    body = "Бірнеше шкала бойынша мәндердің комбинациясы бірегей мінез-құлық үлгісін қалыптастырады."
  ),
  AboutSlide(
    title = "Көпөлшемді куб",
    body = "Сканерленген даыстар виртуалды көпөлшемді кубта орналасады; ұқсас психотиптерді анықтау үшін аймақтар белгіленеді.\n\nҚазір кеңістік 8 аймаққа бөлінген, кеңейту мүмкіндігі бар. Жаңа шкалалар мен алгоритмдер дәлдікті арттырады, ондаған тип пен комбинацияны анықтауға болады."
  ),
  AboutSlide(
    title = "Дауыстық үлгілерді кешенді талдау",
    body = "Талдаушылар микросервистер арқылы негізгі дауыстық параметрлерді бағалайды, содан кейін олар «ноотиптік матрица» — психотиптерді жіктеу жүйесі —мен салыстырылады."
  ),
  AboutSlide(
    title = "Талдау кезеңдері",
    body = "01 · 46 дауыстық талдаушы\nКөптеген параметрді өлшеп толық профиль құрады.\n\n02 · Деректерді топтау\n8, 12, 16 немесе N психотип негізінде жіктеу моделін құру.\n\n03 · Корреляция\nДауыстың физиологиялық дыбысы мен психологиялық аспектілердің байланысы."
  ),
  AboutSlide(
    title = "Негізгі компоненттер",
    body = "• Дауыстың тығыздығы мен энергиясы\n• Музыкалылық пен темп\n• Кеңістік көлемі\n• Үндесім мен атмосфера\n• Сөз құрылымы\n• Анықтық пен өзгергіштігі\n• Микродірілдер мен вибрато\n• Дауыстың конгруэнттілігі"
  ),
  AboutSlide(
    title = "Технология артықшылықтары",
    body = "90% — талдау дәлдігі. Психотип пен мінез-құлық болжамы бойынша жоғары дәлдік.\n\n46 — дауыстық параметр. Көпөлшемді талдау.\n\n3000+ — зерттеу қатысушысы. Технологияны валидациялау үшін кең база."
  ),
  AboutSlide(
    title = "Қосымша артықшылықтар",
    body = "Мінез-құлық болжамы\nДауыстық деректер негізінде сенімді болжам.\n\nӘдістемелерді интеграциялау\nБар психотипологиялық әдістермен үйлесімділік.\n\nЖылдам талдау\nҰзақ тест пен сауалнамасыз дереу нәтиже."
  )
)
