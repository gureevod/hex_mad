# Hex Element Hints Plugin - План улучшений

## Содержание

1. [Анализ текущего состояния](#анализ-текущего-состояния)
2. [Обнаруженные ошибки](#обнаруженные-ошибки)
3. [Требуемые улучшения](#требуемые-улучшения)
4. [Предложенные новые фичи](#предложенные-новые-фичи)
5. [План реализации](#план-реализации)

---

## Анализ текущего состояния

### Текущие возможности плагина

| Функция | Файл | Статус |
|---------|------|--------|
| Inlay hints с русскими названиями | `HexInlayHintsProvider.kt` | ✅ Работает |
| Line markers для Hex элементов | `HexElementLineMarkerProvider.kt` | ✅ Работает |
| Code completion с названиями | `HexElementCompletionContributor.kt` | ⚠️ Ошибка пакета |
| Документация при наведении | `HexElementDocumentationProvider.kt` | ✅ Работает |
| Инспекция отсутствия name | `MissingElementNameInspection.kt` | ✅ Работает |
| Инспекция русского названия | `MissingRussianNameInspection.kt` | ⚠️ Ошибка импорта |
| Quick fix для добавления JavaDoc | `AddJavaDocQuickFix.kt` | ⚠️ Ошибка пакета |
| Настройки плагина | `HexPluginSettings.kt` | ⚠️ Неполные |

### Структура настроек

Текущие настройки в `HexPluginSettings.State`:
- `showInlayHints: Boolean` — показывать ли inlay hints
- `showLineMarkers: Boolean` — показывать ли line markers  
- `enableInspections: Boolean` — включить ли инспекции

Настройки в `HexInlayHintsProvider.Settings` (не подключены к UI):
- `showRussianNames: Boolean` — показывать русские названия
- `showEnglishNames: Boolean` — показывать английские названия
- `showLocators: Boolean` — показывать локаторы

---

## Обнаруженные ошибки

### 1. Несоответствие пакетов (КРИТИЧНО)

**Файл:** [`HexElementCompletionContributor.kt`](main/kotlin/org/sber/hexelementhints/completion/HexElementCompletionContributor.kt:1)

```kotlin
// ❌ Текущий пакет
package com.company.hex.idea.completion

// ✅ Должен быть
package org.sber.hexelementhints.completion
```

**Файл:** [`AddJavaDocQuickFix.kt`](main/kotlin/org/sber/hexelementhints/quickfixes/AddJavaDocQuickFix.kt:1)

```kotlin
// ❌ Текущий пакет
package com.company.hex.idea.quickfixes

// ✅ Должен быть
package org.sber.hexelementhints.quickfixes
```

### 2. Неверный импорт в MissingRussianNameInspection

**Файл:** [`MissingRussianNameInspection.kt`](main/kotlin/org/sber/hexelementhints/inspections/MissingRussianNameInspection.kt:3)

```kotlin
// ❌ Текущий импорт
import com.company.hex.idea.quickfixes.AddJavaDocQuickFix

// ✅ Должен быть
import org.sber.hexelementhints.quickfixes.AddJavaDocQuickFix
```

### 3. Неверные пути базовых классов

**Файл:** [`HexPsiUtils.kt`](main/kotlin/org/sber/hexelementhints/utils/HexPsiUtils.kt:17-19)

```kotlin
// ❌ Текущие значения
const val BASE_PAGE_CLASS = "com.company.hex.ui.pages.BasePage"
const val BASE_COMPONENT_CLASS = "com.company.hex.ui.components.BaseComponent"
const val BASE_ELEMENT_CLASS = "com.company.hex.ui.elements.BaseElement"

// ✅ Должны быть (согласно hex-core-ui)
const val BASE_PAGE_CLASS = "com.company.hex.ui.core.BasePage"
const val BASE_COMPONENT_CLASS = "com.company.hex.ui.core.BaseComponent"
const val BASE_ELEMENT_CLASS = "com.company.hex.ui.core.BaseElement"
```

### 4. Настройки inlay hints не подключены к UI

**Проблема:** `HexInlayHintsProvider.Settings` содержит настройки `showRussianNames`, `showEnglishNames`, `showLocators`, но они не управляются через UI настроек плагина.

**Файл:** [`HexPluginConfigurable.kt`](main/kotlin/org/sber/hexelementhints/settings/HexPluginConfigurable.kt:18-31)

---

## Требуемые улучшения

### 1. Расширение настроек отображения подсказок

**Цель:** Добавить в настройки переключатели для контроля того, что отображать в подсказках.

**Изменения в `HexPluginSettings.kt`:**

```kotlin
data class State(
    var showInlayHints: Boolean = true,
    var showLineMarkers: Boolean = true,
    var enableInspections: Boolean = true,
    
    // Новые настройки для содержимого hint-ов
    var hintShowElementName: Boolean = true,      // @Element(name = "...")
    var hintShowLocator: Boolean = false,         // xpath или css
    var hintShowFieldType: Boolean = false,       // Button, Input, etc.
    var hintShowJavaDoc: Boolean = true,          // Описание из JavaDoc
    
    // Настройки формата отображения
    var hintMaxLocatorLength: Int = 50,           // Макс. длина локатора
    var hintSeparator: String = " • "             // Разделитель между частями
)
```

**Изменения в `HexPluginConfigurable.kt`:**

```kotlin
override fun createComponent(): JComponent {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
    
    // Основные переключатели
    panel.add(JLabel("Основные настройки:"))
    inlayHintsCheckbox = JCheckBox("Показывать inlay hints")
    lineMarkersCheckbox = JCheckBox("Показывать маркеры в gutter")
    inspectionsCheckbox = JCheckBox("Включить инспекции кода")
    
    panel.add(inlayHintsCheckbox)
    panel.add(lineMarkersCheckbox)
    panel.add(inspectionsCheckbox)
    
    panel.add(Box.createVerticalStrut(16))
    panel.add(JSeparator())
    panel.add(Box.createVerticalStrut(8))
    
    // Настройки содержимого hints
    panel.add(JLabel("Содержимое подсказок:"))
    hintShowElementNameCheckbox = JCheckBox("Имя из @Element(name)")
    hintShowJavaDocCheckbox = JCheckBox("Описание из JavaDoc")
    hintShowLocatorCheckbox = JCheckBox("Локатор (xpath/css)")
    hintShowFieldTypeCheckbox = JCheckBox("Тип элемента")
    
    panel.add(hintShowElementNameCheckbox)
    panel.add(hintShowJavaDocCheckbox)
    panel.add(hintShowLocatorCheckbox)
    panel.add(hintShowFieldTypeCheckbox)
    
    // Дополнительные настройки
    panel.add(Box.createVerticalStrut(8))
    panel.add(JLabel("Максимальная длина локатора:"))
    locatorLengthSpinner = JSpinner(SpinnerNumberModel(50, 10, 200, 10))
    panel.add(locatorLengthSpinner)
    
    settingsPanel = panel
    return panel
}
```

---

### 2. Автодополнение при поиске по имени элемента

**Цель:** При вводе `userPage.имя` предлагать поля, у которых `@Element(name = "Имя", ...)`.

**Создать новый провайдер или расширить существующий:**

**Новый файл: `HexElementNameCompletionContributor.kt`**

```kotlin
package org.sber.hexelementhints.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.sber.hexelementhints.utils.HexIcons
import org.sber.hexelementhints.utils.HexPsiUtils

/**
 * Умный автокомплит: при вводе текста ищет поля по имени из @Element(name="...")
 * 
 * Пример: userPage.пас[курсор] → предложит password, если есть @Element(name="Пароль")
 */
class HexElementNameCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            HexElementNameCompletionProvider()
        )
    }
}

class HexElementNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val parent = position.parent
        
        // Получаем текст, который пользователь вводит
        val prefix = result.prefixMatcher.prefix.lowercase()
        if (prefix.isEmpty()) return
        
        // Проверяем, что мы в контексте обращения к полю класса
        if (parent !is PsiReferenceExpression) return
        val qualifier = parent.qualifierExpression ?: return
        val qualifierType = qualifier.type as? PsiClassType ?: return
        val psiClass = qualifierType.resolve() ?: return
        
        // Проверяем, что это Page или Component
        if (!HexPsiUtils.isPageClass(psiClass) && !HexPsiUtils.isComponentClass(psiClass)) {
            return
        }
        
        // Ищем все поля с @Element и фильтруем по name
        psiClass.allFields
            .filter { HexPsiUtils.isHexElementField(it) }
            .forEach { field ->
                val elementName = HexPsiUtils.extractNameFromAnnotation(field)
                val javaDocName = HexPsiUtils.extractRussianNameFromJavaDoc(field)
                
                // Проверяем, содержит ли name или javadoc введенный текст
                val matchesName = elementName?.lowercase()?.contains(prefix) == true
                val matchesJavaDoc = javaDocName?.lowercase()?.contains(prefix) == true
                val matchesFieldName = field.name.lowercase().contains(prefix)
                
                if (matchesName || matchesJavaDoc || matchesFieldName) {
                    result.addElement(createLookupElement(field, elementName, javaDocName))
                }
            }
    }

    private fun createLookupElement(
        field: PsiField, 
        elementName: String?,
        javaDocName: String?
    ): LookupElementBuilder {
        val displayName = elementName ?: javaDocName
        val elementType = HexPsiUtils.getElementType(field)
        val locator = extractShortLocator(field)
        
        var builder = LookupElementBuilder.create(field, field.name)
            .withIcon(HexIcons.ELEMENT)
            .withTypeText(elementType, true)
            .bold()
        
        // Добавляем название как tail text
        if (displayName != null) {
            builder = builder.withTailText(" — $displayName", true)
        }
        
        // Добавляем lookup string для поиска по русскому названию
        if (elementName != null) {
            builder = builder.withLookupString(elementName)
        }
        if (javaDocName != null && javaDocName != elementName) {
            builder = builder.withLookupString(javaDocName)
        }
        
        return builder
    }
    
    private fun extractShortLocator(field: PsiField): String? {
        val annotation = field.getAnnotation(HexPsiUtils.ELEMENT_ANNOTATION)
            ?: field.getAnnotation(HexPsiUtils.ELEMENTS_ANNOTATION)
            ?: return null

        val xpath = annotation.findAttributeValue("xpath")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        val css = annotation.findAttributeValue("css")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        return xpath?.take(40) ?: css?.take(40)
    }
}
```

**Регистрация в `plugin.xml`:**

```xml
<completion.contributor
    language="JAVA"
    implementationClass="org.sber.hexelementhints.completion.HexElementNameCompletionContributor"
    order="before HexElementCompletionContributor"/>
```

---

### 3. Пометка кода документации на удаление

**Файлы для удаления:**

1. `HexElementDocumentationProvider.kt` — весь файл
2. Удалить регистрацию в `plugin.xml`:
   ```xml
   <!-- Удалить этот блок -->
   <lang.documentationProvider
       language="JAVA"
       implementationClass="org.sber.hexelementhints.documentation.HexElementDocumentationProvider"
       order="first"/>
   ```

---

### 4. Показ @Element info при вызове методов

**Цель:** При написании `userPage.identityDoc.getText()` показывать подсказку с информацией из `@Element(name = 'Паспорт', xpath = '//someXpath')`.

**Подход 1: Расширение Inlay Hints для method calls**

**Изменения в `HexInlayHintsCollector.kt`:**

```kotlin
@Suppress("UnstableApiUsage")
class HexInlayHintsCollector(
    editor: Editor,
    private val settings: HexInlayHintsProvider.Settings
) : FactoryInlayHintsCollector(editor) {

    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        // Существующая логика для полей
        if (element is PsiField && HexPsiUtils.isHexElementField(element)) {
            handleFieldHint(element, sink)
        }
        
        // Новая логика для вызовов методов
        if (element is PsiMethodCallExpression) {
            handleMethodCallHint(element, sink)
        }
        
        // Новая логика для обращений к полям в коде
        if (element is PsiReferenceExpression) {
            handleReferenceHint(element, sink)
        }
        
        return true
    }
    
    private fun handleFieldHint(field: PsiField, sink: InlayHintsSink) {
        val pluginSettings = HexPluginSettings.getInstance()
        if (!pluginSettings.showInlayHints) return
        
        val hints = buildHintText(field)
        if (hints.isNotEmpty()) {
            val presentation = createHintPresentation(hints)
            val offset = field.nameIdentifier?.textRange?.endOffset ?: return
            
            sink.addInlineElement(
                offset,
                relatesToPrecedingText = true,
                presentation = presentation,
                placeAtTheEndOfLine = false
            )
        }
    }
    
    private fun handleMethodCallHint(methodCall: PsiMethodCallExpression, sink: InlayHintsSink) {
        val pluginSettings = HexPluginSettings.getInstance()
        if (!pluginSettings.showInlayHints) return
        
        // Получаем qualifier (например, userPage.identityDoc)
        val qualifier = methodCall.methodExpression.qualifierExpression ?: return
        
        // Ищем PsiField, на который ссылается qualifier
        val field = resolveToHexField(qualifier) ?: return
        
        // Получаем информацию из @Element
        val elementName = HexPsiUtils.extractNameFromAnnotation(field)
        val locator = extractShortLocator(field)
        
        if (elementName != null || locator != null) {
            val hintParts = mutableListOf<String>()
            elementName?.let { hintParts.add(it) }
            // locator?.let { hintParts.add("📍 $it") } // опционально
            
            val hint = hintParts.joinToString(" ")
            if (hint.isNotEmpty()) {
                val presentation = factory.smallText("  // $hint")
                val offset = methodCall.textRange.endOffset
                
                sink.addInlineElement(
                    offset,
                    relatesToPrecedingText = true,
                    presentation = presentation,
                    placeAtTheEndOfLine = true
                )
            }
        }
    }
    
    private fun handleReferenceHint(ref: PsiReferenceExpression, sink: InlayHintsSink) {
        val pluginSettings = HexPluginSettings.getInstance()
        if (!pluginSettings.showInlayHints) return
        
        // Проверяем, что это обращение к полю элемента (не само определение)
        val resolved = ref.resolve() as? PsiField ?: return
        if (!HexPsiUtils.isHexElementField(resolved)) return
        
        // Проверяем, что это не определение поля (PsiDeclarationStatement)
        if (ref.parent is PsiField) return
        
        val elementName = HexPsiUtils.extractNameFromAnnotation(resolved)
            ?: HexPsiUtils.extractRussianNameFromJavaDoc(resolved)
            ?: return
        
        // Показываем hint только если это chain call (userPage.field.method())
        val parent = ref.parent
        if (parent is PsiReferenceExpression && parent.qualifierExpression == ref) {
            val presentation = factory.smallText(" /* $elementName */")
            val offset = ref.textRange.endOffset
            
            sink.addInlineElement(
                offset,
                relatesToPrecedingText = true,
                presentation = presentation,
                placeAtTheEndOfLine = false
            )
        }
    }
    
    private fun resolveToHexField(expr: PsiExpression): PsiField? {
        return when (expr) {
            is PsiReferenceExpression -> {
                val resolved = expr.resolve()
                if (resolved is PsiField && HexPsiUtils.isHexElementField(resolved)) {
                    resolved
                } else null
            }
            else -> null
        }
    }
    
    private fun extractShortLocator(field: PsiField): String? {
        val annotation = field.getAnnotation(HexPsiUtils.ELEMENT_ANNOTATION)
            ?: field.getAnnotation(HexPsiUtils.ELEMENTS_ANNOTATION)
            ?: return null

        val xpath = annotation.findAttributeValue("xpath")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        val css = annotation.findAttributeValue("css")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        val maxLen = HexPluginSettings.getInstance().myState.hintMaxLocatorLength ?: 50
        return xpath?.take(maxLen)?.let { "xpath: $it${if (xpath.length > maxLen) "..." else ""}" }
            ?: css?.take(maxLen)?.let { "css: $it${if (css.length > maxLen) "..." else ""}" }
    }
    
    // ... existing buildHintText and createHintPresentation
}
```

---

## Предложенные новые фичи

### 1. Go to Element Definition

**Описание:** Навигация от использования элемента в тесте к его определению в Page Object.

```kotlin
// Новый файл: HexElementGotoDeclarationHandler.kt
class HexElementGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        // При Ctrl+Click на field reference переходит к @Element определению
        val ref = sourceElement?.parent as? PsiReferenceExpression ?: return null
        val field = ref.resolve() as? PsiField ?: return null
        
        if (HexPsiUtils.isHexElementField(field)) {
            return arrayOf(field)
        }
        return null
    }
}
```

### 2. Find Usages by Element Name

**Описание:** Поиск всех использований элемента по его русскому имени.

```kotlin
// Интеграция с Find Usages — показывать имя элемента в результатах
class HexElementUsageTypeProvider : UsageTypeProvider {
    override fun getUsageType(element: PsiElement): UsageType? {
        val field = element.parent as? PsiField ?: return null
        if (!HexPsiUtils.isHexElementField(field)) return null
        
        val name = HexPsiUtils.extractElementName(field) ?: return null
        return UsageType { "UI Element: $name" }
    }
}
```

### 3. Live Template для Element

**Описание:** Автоматическая генерация `@Element` аннотации с полями.

```kotlin
// Файл: HexLiveTemplateProvider.kt
// Live template: element → @Element(name = "$NAME$", xpath = "$XPATH$") public $TYPE$ $FIELD$;
```

**Добавить в ресурсы шаблоны:**

```xml
<!-- templates/HexElement.xml -->
<templateSet group="Hex">
    <template name="element" 
              value="@Element(name = &quot;$NAME$&quot;, xpath = &quot;$XPATH$&quot;)&#10;public $TYPE$ $FIELD$;"
              description="Create Hex UI Element"
              toReformat="true"
              toShortenFQNames="true">
        <variable name="NAME" expression="" defaultValue="&quot;Element Name&quot;" alwaysStopAt="true"/>
        <variable name="XPATH" expression="" defaultValue="&quot;//xpath&quot;" alwaysStopAt="true"/>
        <variable name="TYPE" expression="complete()" defaultValue="Input" alwaysStopAt="true"/>
        <variable name="FIELD" expression="camelCase(NAME)" defaultValue="fieldName" alwaysStopAt="true"/>
        <context>
            <option name="JAVA_DECLARATION" value="true"/>
        </context>
    </template>
    
    <template name="page"
              value="@Page(url = &quot;$URL$&quot;, title = &quot;$TITLE$&quot;)&#10;public class $NAME$ extends BasePage {&#10;    $END$&#10;}"
              description="Create Hex Page Object"
              toReformat="true">
        <variable name="URL" expression="" defaultValue="&quot;/path&quot;" alwaysStopAt="true"/>
        <variable name="TITLE" expression="" defaultValue="&quot;Page Title&quot;" alwaysStopAt="true"/>
        <variable name="NAME" expression="fileNameWithoutExtension()" defaultValue="PageName" alwaysStopAt="true"/>
        <context>
            <option name="JAVA_DECLARATION" value="true"/>
        </context>
    </template>
    
    <template name="comp"
              value="@Component(name = &quot;$NAME$&quot;, root = &quot;$XPATH$&quot;)&#10;public $TYPE$ $FIELD$;"
              description="Create Hex Component Field"
              toReformat="true">
        <variable name="NAME" expression="" defaultValue="&quot;Component Name&quot;" alwaysStopAt="true"/>
        <variable name="XPATH" expression="" defaultValue="&quot;//xpath&quot;" alwaysStopAt="true"/>
        <variable name="TYPE" expression="complete()" defaultValue="HeaderComponent" alwaysStopAt="true"/>
        <variable name="FIELD" expression="camelCase(NAME)" defaultValue="component" alwaysStopAt="true"/>
        <context>
            <option name="JAVA_DECLARATION" value="true"/>
        </context>
    </template>
</templateSet>
```

### 4. Element Structure View

**Описание:** Отображение структуры Page Object с элементами в виде дерева.

```kotlin
// Файл: HexStructureViewExtension.kt
class HexStructureViewExtension : StructureViewExtension {
    override fun getType(): Class<out PsiElement> = PsiClass::class.java
    
    override fun getChildren(parent: PsiElement): Array<StructureViewTreeElement> {
        val psiClass = parent as? PsiClass ?: return emptyArray()
        if (!HexPsiUtils.isPageClass(psiClass) && !HexPsiUtils.isComponentClass(psiClass)) {
            return emptyArray()
        }
        
        return psiClass.fields
            .filter { HexPsiUtils.isHexElementField(it) }
            .map { HexElementTreeElement(it) }
            .toTypedArray()
    }
}

class HexElementTreeElement(private val field: PsiField) : StructureViewTreeElement {
    override fun getPresentation(): ItemPresentation {
        val name = HexPsiUtils.extractElementName(field) ?: field.name
        val type = HexPsiUtils.getElementType(field)
        
        return object : ItemPresentation {
            override fun getPresentableText() = "$name ($type)"
            override fun getIcon(unused: Boolean) = HexIcons.ELEMENT
            override fun getLocationString() = field.name
        }
    }
    
    override fun getValue() = field
    override fun getChildren() = emptyArray<TreeElement>()
    override fun navigate(requestFocus: Boolean) = field.navigate(requestFocus)
    override fun canNavigate() = field.canNavigate()
    override fun canNavigateToSource() = field.canNavigateToSource()
}
```

### 5. XPath/CSS Validator

**Описание:** Валидация синтаксиса XPath/CSS прямо в IDE.

```kotlin
// Файл: inspections/InvalidLocatorInspection.kt
class InvalidLocatorInspection : AbstractBaseJavaLocalInspectionTool() {
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitAnnotation(annotation: PsiAnnotation) {
                if (annotation.qualifiedName != HexPsiUtils.ELEMENT_ANNOTATION &&
                    annotation.qualifiedName != HexPsiUtils.ELEMENTS_ANNOTATION) return
                
                val xpath = annotation.findAttributeValue("xpath")
                    ?.let { (it as? PsiLiteralExpression)?.value as? String }
                
                if (xpath != null && !isValidXPath(xpath)) {
                    holder.registerProblem(
                        annotation.findAttributeValue("xpath")!!,
                        "Некорректный XPath синтаксис",
                        ProblemHighlightType.ERROR
                    )
                }
            }
            
            private fun isValidXPath(xpath: String): Boolean {
                return try {
                    javax.xml.xpath.XPathFactory.newInstance()
                        .newXPath()
                        .compile(xpath)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }
    }
}
```

### 6. Duplicate Locator Detection

**Описание:** Обнаружение дублирующихся локаторов в проекте.

```kotlin
// Файл: inspections/DuplicateLocatorInspection.kt
class DuplicateLocatorInspection : GlobalInspectionTool() {
    
    override fun runInspection(
        scope: AnalysisScope,
        manager: InspectionManager,
        globalContext: GlobalInspectionContext,
        problemDescriptionsProcessor: ProblemDescriptionsProcessor
    ) {
        val locatorsMap = mutableMapOf<String, MutableList<PsiField>>()
        
        // Собираем все локаторы
        scope.accept { file ->
            if (file is PsiJavaFile) {
                file.classes.forEach { psiClass ->
                    psiClass.fields.forEach { field ->
                        if (HexPsiUtils.isHexElementField(field)) {
                            val locator = extractLocator(field)
                            if (locator != null) {
                                locatorsMap.getOrPut(locator) { mutableListOf() }.add(field)
                            }
                        }
                    }
                }
            }
            true
        }
        
        // Регистрируем проблемы для дубликатов
        locatorsMap.filter { it.value.size > 1 }.forEach { (locator, fields) ->
            fields.forEach { field ->
                val problem = manager.createProblemDescriptor(
                    field,
                    "Дублирующийся локатор: $locator (${fields.size} вхождений)",
                    null as LocalQuickFix?,
                    ProblemHighlightType.WARNING,
                    true
                )
                problemDescriptionsProcessor.addProblemElement(
                    globalContext.refManager.getReference(field),
                    problem
                )
            }
        }
    }
}
```

### 7. Quick Navigation Panel

**Описание:** Floating panel со списком всех элементов текущей страницы.

```kotlin
// Файл: toolwindow/HexElementsToolWindow.kt
class HexElementsToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = HexElementsPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "Elements", false)
        toolWindow.contentManager.addContent(content)
    }
}

class HexElementsPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val list = JBList<ElementInfo>()
    
    init {
        add(JBScrollPane(list), BorderLayout.CENTER)
        
        // Обновляем при смене файла
        project.messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    updateElements()
                }
            }
        )
    }
    
    private fun updateElements() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val psiFile = PsiDocumentManager.getInstance(project)
            .getPsiFile(editor.document) as? PsiJavaFile ?: return
        
        val elements = mutableListOf<ElementInfo>()
        psiFile.classes.forEach { psiClass ->
            if (HexPsiUtils.isPageClass(psiClass) || HexPsiUtils.isComponentClass(psiClass)) {
                psiClass.fields.forEach { field ->
                    if (HexPsiUtils.isHexElementField(field)) {
                        elements.add(ElementInfo(
                            name = HexPsiUtils.extractElementName(field) ?: field.name,
                            fieldName = field.name,
                            type = HexPsiUtils.getElementType(field),
                            field = field
                        ))
                    }
                }
            }
        }
        
        list.model = CollectionListModel(elements)
    }
    
    data class ElementInfo(
        val name: String,
        val fieldName: String,
        val type: String,
        val field: PsiField
    ) {
        override fun toString() = "$name ($type) - $fieldName"
    }
}
```

### 8. Element Copy with Locator

**Описание:** Копирование элемента с локатором для использования в других местах.

```kotlin
// Файл: actions/CopyElementLocatorAction.kt
class CopyElementLocatorAction : AnAction("Copy Element Locator") {
    override fun actionPerformed(e: AnActionEvent) {
        val field = getSelectedField(e) ?: return
        val locator = HexPsiUtils.extractLocator(field) ?: return
        
        CopyPasteManager.getInstance().setContents(StringSelection(locator))
        
        // Показываем уведомление
        val notification = Notification(
            "Hex",
            "Локатор скопирован",
            locator,
            NotificationType.INFORMATION
        )
        Notifications.Bus.notify(notification, e.project)
    }
    
    override fun update(e: AnActionEvent) {
        val field = getSelectedField(e)
        e.presentation.isEnabledAndVisible = field != null && HexPsiUtils.isHexElementField(field)
    }
    
    private fun getSelectedField(e: AnActionEvent): PsiField? {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return null
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return null
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)
        return element?.parent as? PsiField
    }
}
```

---

## План реализации

### Фаза 1: Исправление ошибок (Приоритет: ВЫСОКИЙ)

| № | Задача | Файл | Сложность |
|---|--------|------|-----------|
| 1.1 | Исправить пакет `HexElementCompletionContributor` | `completion/HexElementCompletionContributor.kt` | Легко |
| 1.2 | Исправить пакет `AddJavaDocQuickFix` | `quickfixes/AddJavaDocQuickFix.kt` | Легко |
| 1.3 | Исправить импорт в `MissingRussianNameInspection` | `inspections/MissingRussianNameInspection.kt` | Легко |
| 1.4 | Исправить пути базовых классов | `utils/HexPsiUtils.kt` | Легко |

### Фаза 2: Улучшение настроек (Приоритет: СРЕДНИЙ)

| № | Задача | Файл | Сложность |
|---|--------|------|-----------|
| 2.1 | Расширить State с новыми настройками | `settings/HexPluginSettings.kt` | Легко |
| 2.2 | Добавить UI для новых настроек | `settings/HexPluginConfigurable.kt` | Средне |
| 2.3 | Подключить настройки к HintsCollector | `hints/HexInlayHintsProvider.kt` | Средне |

### Фаза 3: Автодополнение по имени (Приоритет: ВЫСОКИЙ)

| № | Задача | Файл | Сложность |
|---|--------|------|-----------|
| 3.1 | Создать новый CompletionContributor | `completion/HexElementNameCompletionContributor.kt` | Средне |
| 3.2 | Зарегистрировать в plugin.xml | `META-INF/plugin.xml` | Легко |

### Фаза 4: Hints для method calls (Приоритет: СРЕДНИЙ)

| № | Задача | Файл | Сложность |
|---|--------|------|-----------|
| 4.1 | Добавить обработку MethodCallExpression | `hints/HexInlayHintsProvider.kt` | Средне |
| 4.2 | Добавить обработку ReferenceExpression | `hints/HexInlayHintsProvider.kt` | Средне |

### Фаза 5: Удаление документации (Приоритет: НИЗКИЙ - по запросу пользователя)

| № | Задача | Файл | Сложность |
|---|--------|------|-----------|
| 5.1 | Удалить HexElementDocumentationProvider | `documentation/` | Легко |
| 5.2 | Удалить регистрацию из plugin.xml | `META-INF/plugin.xml` | Легко |

### Фаза 6: Новые фичи (Приоритет: НИЗКИЙ)

| № | Задача | Описание | Сложность |
|---|--------|----------|-----------|
| 6.1 | Go to Element Definition | Навигация к определению | Средне |
| 6.2 | Live Templates | Шаблоны для Element/Page | Средне |
| 6.3 | Structure View Extension | Структура Page Object | Сложно |
| 6.4 | XPath Validator | Валидация локаторов | Средне |
| 6.5 | Duplicate Locator Detection | Поиск дубликатов | Сложно |
| 6.6 | Elements Tool Window | Панель элементов | Сложно |
| 6.7 | Copy Locator Action | Копирование локатора | Легко |

---

## Итого

### Ошибки для немедленного исправления: 4
### Улучшения по запросу пользователя: 4
### Новые предложенные фичи: 8

**Оценка трудозатрат:**
- Фаза 1 (ошибки): ~1-2 часа
- Фаза 2 (настройки): ~2-3 часа  
- Фаза 3 (автодополнение): ~3-4 часа
- Фаза 4 (method hints): ~2-3 часа
- Фаза 5 (удаление): ~30 минут
- Фаза 6 (новые фичи): ~10-15 часов

**Общая оценка:** ~20-30 часов