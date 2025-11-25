package com.company.hex.ui.collections;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import com.codeborne.selenide.WebElementsCondition;
import com.company.hex.core.logging.HexLoggerFactory;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.core.UiContext;
import com.company.hex.ui.factory.ElementFactory;
import io.qameta.allure.Step;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Типобезопасная коллекция UI элементов с "живой" резолвацией и функциональным API.
 * Предоставляет удобные методы для фильтрации, поиска, трансформации и проверки коллекций элементов.
 * 
 * <p>Основные возможности:</p>
 * <ul>
 *   <li>Ленивая резолвация элементов (всегда отражает текущее состояние DOM)</li>
 *   <li>Типобезопасность через дженерики</li>
 *   <li>Функциональный API (filter, map, forEach)</li>
 *   <li>Автоматические Allure steps с агрегацией</li>
 *   <li>Контекстное логирование (page + component + collection)</li>
 *   <li>Поддержка Selenide условий и Java предикатов</li>
 * </ul>
 * 
 * @param <T> тип элементов в коллекции (должен наследоваться от BaseElement)
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class ElementList<T extends BaseElement> implements Iterable<T> {
    
    private final Logger logger;
    private final String name;
    private final Supplier<ElementsCollection> resolver;
    private final Class<T> type;
    private final UiContext context;
    
    // Для параметризованных локаторов
    private final String locatorTemplate;
    private final boolean isXpath;
    
    /**
     * Создать ElementList с указанными параметрами.
     *
     * @param name имя коллекции для логирования и Allure
     * @param resolver поставщик ElementsCollection для ленивой резолвации
     * @param type класс элементов в коллекции
     * @param context контекст UI (страница + компонент)
     */
    public ElementList(String name, Supplier<ElementsCollection> resolver, Class<T> type, UiContext context) {
        this(name, resolver, type, context, null, false);
    }
    
    /**
     * Создать ElementList с поддержкой параметризованных локаторов.
     *
     * @param name имя коллекции для логирования и Allure
     * @param resolver поставщик ElementsCollection для ленивой резолвации
     * @param type класс элементов в коллекции
     * @param context контекст UI (страница + компонент)
     * @param locatorTemplate шаблон локатора с плейсхолдерами (может быть null)
     * @param isXpath true если локатор является XPath
     */
    public ElementList(String name, Supplier<ElementsCollection> resolver, Class<T> type,
                      UiContext context, String locatorTemplate, boolean isXpath) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя коллекции не может быть null или пустым");
        }
        if (resolver == null) {
            throw new IllegalArgumentException("Resolver не может быть null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Тип элемента не может быть null");
        }
        if (context == null) {
            throw new IllegalArgumentException("Контекст не может быть null");
        }
        
        this.name = name;
        this.resolver = resolver;
        this.type = type;
        this.context = context;
        this.locatorTemplate = locatorTemplate;
        this.isXpath = isXpath;
        this.logger = HexLoggerFactory.getUiLogger(ElementList.class);
    }
    
    /**
     * Получить имя коллекции.
     * 
     * @return имя коллекции
     */
    public String getName() {
        return name;
    }
    
    // ==================== Методы доступа ====================
    
    /**
     * Получить размер коллекции.
     * 
     * @return количество элементов
     */
    @Step("Получить размер '{this.name}'")
    public int size() {
        logger.debug("Получение размера '{}' {}", name, context.getFullContext());
        int size = resolver.get().size();
        logger.debug("Размер '{}': {}", name, size);
        return size;
    }
    
    /**
     * Проверить, пуста ли коллекция.
     * 
     * @return true если коллекция пуста
     */
    public boolean isEmpty() {
        return size() == 0;
    }
    
    /**
     * Получить элемент по индексу (0-based).
     * 
     * @param index индекс элемента
     * @return элемент
     * @throws IndexOutOfBoundsException если индекс вне диапазона
     */
    @Step("Получить элемент [{index}] из '{this.name}'")
    public T get(int index) {
        logger.debug("Получение элемента [{}] из '{}' {}", index, name, context.getFullContext());
        
        int currentSize = size();
        if (index < 0 || index >= currentSize) {
            String errorMsg = String.format(
                "Индекс %d вне диапазона для '%s' (размер=%d) %s",
                index, name, currentSize, context.getFullContext());
            logger.error(errorMsg);
            throw new IndexOutOfBoundsException(errorMsg);
        }
        
        SelenideElement element = resolver.get().get(index);
        String itemName = String.format("%s[%d]", name, index);
        T wrappedElement = ElementFactory.create(type, itemName, element);
        wrappedElement.setPageName(context.getPageName());
        wrappedElement.setComponentName(context.getComponentName());
        
        logger.trace("Элемент [{}] из '{}' успешно получен", index, name);
        return wrappedElement;
    }
    
    /**
     * Синоним для get(index).
     * 
     * @param index индекс элемента
     * @return элемент
     */
    public T nth(int index) {
        return get(index);
    }
    
    /**
     * Получить первый элемент коллекции.
     * 
     * @return первый элемент
     * @throws IndexOutOfBoundsException если коллекция пуста
     */
    @Step("Получить первый элемент из '{this.name}'")
    public T first() {
        logger.debug("Получение первого элемента из '{}' {}", name, context.getFullContext());
        return get(0);
    }
    
    /**
     * Получить последний элемент коллекции.
     * 
     * @return последний элемент
     * @throws IndexOutOfBoundsException если коллекция пуста
     */
    @Step("Получить последний элемент из '{this.name}'")
    public T last() {
        logger.debug("Получение последнего элемента из '{}' {}", name, context.getFullContext());
        int lastIndex = size() - 1;
        return get(lastIndex);
    }
    
    /**
     * Получить единственный элемент коллекции.
     * Ожидает ровно один элемент, иначе выбрасывает исключение.
     * 
     * @return единственный элемент
     * @throws IllegalStateException если элементов != 1
     */
    @Step("Получить единственный элемент из '{this.name}'")
    public T single() {
        logger.debug("Получение единственного элемента из '{}' {}", name, context.getFullContext());
        
        int currentSize = size();
        if (currentSize == 0) {
            String errorMsg = String.format(
                "Ожидался ровно один элемент в '%s', но не найдено ни одного %s",
                name, context.getFullContext());
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        if (currentSize > 1) {
            String errorMsg = String.format(
                "Ожидался ровно один элемент в '%s', но найдено %d %s",
                name, currentSize, context.getFullContext());
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        
        return get(0);
    }
    
    // ==================== Фильтрация и поиск ====================
    
    /**
     * Отфильтровать коллекцию по Selenide условию (с ожиданием).
     * Возвращает новую "живую" коллекцию.
     *
     * @param condition Selenide условие
     * @return новая отфильтрованная коллекция
     */
    @Step("Фильтровать '{this.name}' по условию {condition}")
    public ElementList<T> filterBy(WebElementCondition condition) {
        logger.info("Фильтрация '{}' по условию {} {}", name, condition, context.getFullContext());
        
        Supplier<ElementsCollection> filteredResolver = () -> resolver.get().filter(condition);
        ElementList<T> filtered = new ElementList<>(name, filteredResolver, type, context);
        
        logger.debug("Фильтрация '{}' завершена", name);
        return filtered;
    }
    
    /**
     * Найти первый элемент, соответствующий Selenide условию.
     *
     * @param condition Selenide условие
     * @return Optional с найденным элементом или empty
     */
    @Step("Найти в '{this.name}' элемент по условию {condition}")
    public Optional<T> findBy(WebElementCondition condition) {
        logger.info("Поиск в '{}' по условию {} {}", name, condition, context.getFullContext());
        
        try {
            SelenideElement found = resolver.get().findBy(condition);
            if (found.exists()) {
                String itemName = String.format("%s[найденный]", name);
                T wrappedElement = ElementFactory.create(type, itemName, found);
                wrappedElement.setPageName(context.getPageName());
                wrappedElement.setComponentName(context.getComponentName());
                
                logger.debug("Элемент найден в '{}'", name);
                return Optional.of(wrappedElement);
            }
        } catch (Exception e) {
            logger.debug("Элемент не найден в '{}': {}", name, e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Отфильтровать коллекцию по Java предикату (работает по снэпшоту).
     * Материализует коллекцию и применяет предикат.
     * 
     * @param predicate Java предикат
     * @return новая коллекция с отфильтрованными элементами
     */
    @Step("Фильтровать '{this.name}' по предикату")
    public ElementList<T> filter(Predicate<T> predicate) {
        logger.info("Фильтрация '{}' по Java предикату {}", name, context.getFullContext());
        
        List<T> snapshot = snapshot();
        logger.debug("Снэпшот '{}': размер={}", name, snapshot.size());
        
        List<T> filtered = snapshot.stream()
            .filter(predicate)
            .collect(Collectors.toList());
        
        logger.debug("После фильтрации '{}': размер={}", name, filtered.size());
        
        // Создаем новую коллекцию из отфильтрованных элементов
        return new SnapshotElementList<>(name, filtered, type, context);
    }
    
    /**
     * Найти первый элемент, соответствующий Java предикату (работает по снэпшоту).
     * 
     * @param predicate Java предикат
     * @return Optional с найденным элементом или empty
     */
    @Step("Найти первый в '{this.name}' по предикату")
    public Optional<T> findFirst(Predicate<T> predicate) {
        logger.info("Поиск первого в '{}' по Java предикату {}", name, context.getFullContext());
        
        Optional<T> result = stream().filter(predicate).findFirst();
        
        if (result.isPresent()) {
            logger.debug("Элемент найден в '{}'", name);
        } else {
            logger.debug("Элемент не найден в '{}'", name);
        }
        
        return result;
    }
    
    // ==================== Потоки и трансформации ====================
    
    /**
     * Получить Stream элементов коллекции.
     * 
     * @return Stream элементов
     */
    public Stream<T> stream() {
        return IntStream.range(0, size())
            .mapToObj(this::get);
    }
    
    /**
     * Применить функцию к каждому элементу и получить Stream результатов.
     * 
     * @param <R> тип результата
     * @param mapper функция трансформации
     * @return Stream результатов
     */
    @Step("Трансформировать '{this.name}'")
    public <R> Stream<R> map(Function<T, R> mapper) {
        logger.debug("Трансформация '{}' {}", name, context.getFullContext());
        return stream().map(mapper);
    }
    
    /**
     * Выполнить действие для каждого элемента коллекции.
     *
     * @param action действие для выполнения
     */
    @Step("Выполнить действие для каждого элемента '{this.name}'")
    public void forEachElement(Consumer<T> action) {
        logger.info("Выполнение действия для каждого элемента '{}' {}", name, context.getFullContext());
        
        int size = size();
        logger.debug("Обработка {} элементов из '{}'", size, name);
        
        for (int i = 0; i < size; i++) {
            logger.trace("Обработка элемента [{}] из '{}'", i, name);
            action.accept(get(i));
        }
        
        logger.debug("Обработка всех элементов '{}' завершена", name);
    }
    
    // ==================== Утилиты ====================
    
    /**
     * Получить тексты всех элементов коллекции.
     * 
     * @return список текстов
     */
    @Step("Получить тексты из '{this.name}'")
    public List<String> texts() {
        logger.debug("Получение текстов из '{}' {}", name, context.getFullContext());
        List<String> texts = resolver.get().texts();
        logger.debug("Получено {} текстов из '{}'", texts.size(), name);
        return texts;
    }
    
    /**
     * Получить значения атрибута для всех элементов.
     * 
     * @param attributeName имя атрибута
     * @return список значений атрибута
     */
    @Step("Получить атрибуты '{attributeName}' из '{this.name}'")
    public List<String> attributes(String attributeName) {
        logger.debug("Получение атрибутов '{}' из '{}' {}", attributeName, name, context.getFullContext());
        
        ElementsCollection collection = resolver.get();
        List<String> attributes = new ArrayList<>();
        for (SelenideElement el : collection) {
            attributes.add(el.getAttribute(attributeName));
        }
        
        logger.debug("Получено {} значений атрибута '{}' из '{}'", attributes.size(), attributeName, name);
        return attributes;
    }
    
    /**
     * Получить значения (value) всех элементов.
     * Сахар для attributes("value").
     * 
     * @return список значений
     */
    @Step("Получить значения из '{this.name}'")
    public List<String> values() {
        logger.debug("Получение значений из '{}' {}", name, context.getFullContext());
        return attributes("value");
    }
    
    // ==================== Assertions ====================
    
    /**
     * Проверить, что коллекция соответствует условиям.
     *
     * @param conditions условия для проверки
     * @return this для fluent API
     */
    @Step("'{this.name}' должна соответствовать условиям")
    public ElementList<T> shouldHave(WebElementsCondition... conditions) {
        logger.info("Проверка '{}' на соответствие условиям {}", name, context.getFullContext());
        
        for (WebElementsCondition condition : conditions) {
            logger.debug("Проверка '{}': {}", name, condition);
            resolver.get().shouldHave(condition);
        }
        
        return this;
    }
    
    /**
     * Проверить, что коллекция имеет указанный размер.
     * 
     * @param expected ожидаемый размер
     * @return this для fluent API
     */
    @Step("'{this.name}' должна иметь размер {expected}")
    public ElementList<T> shouldHaveSize(int expected) {
        logger.info("Проверка размера '{}' = {} {}", name, expected, context.getFullContext());
        int actualSize = size();
        if (actualSize != expected) {
            String errorMsg = String.format(
                "Ожидался размер %d для '%s', но получен %d %s",
                expected, name, actualSize, context.getFullContext());
            logger.error(errorMsg);
            throw new AssertionError(errorMsg);
        }
        return this;
    }
    
    /**
     * Проверить, что коллекция пуста.
     * 
     * @return this для fluent API
     */
    @Step("'{this.name}' должна быть пустой")
    public ElementList<T> shouldBeEmpty() {
        logger.info("Проверка что '{}' пуста {}", name, context.getFullContext());
        int actualSize = size();
        if (actualSize != 0) {
            String errorMsg = String.format(
                "Ожидалась пустая коллекция '%s', но размер = %d %s",
                name, actualSize, context.getFullContext());
            logger.error(errorMsg);
            throw new AssertionError(errorMsg);
        }
        return this;
    }
    
    /**
     * Проверить, что коллекция не пуста.
     * 
     * @return this для fluent API
     */
    @Step("'{this.name}' не должна быть пустой")
    public ElementList<T> shouldNotBeEmpty() {
        logger.info("Проверка что '{}' не пуста {}", name, context.getFullContext());
        int actualSize = size();
        if (actualSize == 0) {
            String errorMsg = String.format(
                "Ожидалась непустая коллекция '%s' %s",
                name, context.getFullContext());
            logger.error(errorMsg);
            throw new AssertionError(errorMsg);
        }
        return this;
    }
    
    // ==================== "Люки" ====================
    
    /**
     * Получить базовую Selenide коллекцию.
     * Используйте только если необходим прямой доступ к Selenide API.
     * 
     * @return ElementsCollection
     */
    public ElementsCollection asSelenide() {
        logger.debug("Получение Selenide коллекции для '{}'", name);
        return resolver.get();
    }
    
    /**
     * Материализовать коллекцию в список элементов (снэпшот).
     * Фиксирует текущее состояние коллекции.
     * 
     * @return список элементов
     */
    @Step("Материализовать '{this.name}'")
    public List<T> snapshot() {
        logger.debug("Создание снэпшота '{}' {}", name, context.getFullContext());
        
        int size = size();
        List<T> snapshot = new ArrayList<>(size);
        
        for (int i = 0; i < size; i++) {
            snapshot.add(get(i));
        }
        
        logger.debug("Снэпшот '{}' создан: размер={}", name, snapshot.size());
        return snapshot;
    }
    
    // ==================== Параметризация ====================
    
    /**
     * Создать новую коллекцию с подставленными параметрами в локатор.
     * Используется для динамических локаторов с плейсхолдерами.
     * Параметры передаются парами: "имя", "значение", "имя", "значение", ...
     *
     * <p>Пример:</p>
     * <pre>
     * {@code
     * @Elements(name = "User Rows", xpath = "//tr[@data-status='{status}'][@data-role='{role}']")
     * ElementList<TextElement> userRows;
     *
     * // Использование
     * userRows.resolve("status", "active", "role", "admin").shouldHaveSize(5);
     * }
     * </pre>
     *
     * @param nameValuePairs пары имя-значение для подстановки
     * @return новая коллекция с подставленными параметрами
     * @throws UnsupportedOperationException если коллекция не имеет шаблона локатора
     * @throws IllegalArgumentException если количество параметров нечетное
     */
    public ElementList<T> resolve(String... nameValuePairs) {
        if (locatorTemplate == null) {
            throw new UnsupportedOperationException(
                String.format("Метод resolve() доступен только для коллекций с параметризованным локатором. " +
                    "Коллекция '%s' не имеет шаблона локатора.", name));
        }
        
        // Преобразуем varargs в Map
        if (nameValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException(
                "Параметры должны быть парами: имя1, значение1, имя2, значение2, ...");
        }
        
        Map<String, String> params = new java.util.HashMap<>();
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            params.put(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        
        return resolve(params);
    }
    
    /**
     * Создать новую коллекцию с подставленными параметрами из Map.
     *
     * <p>Пример:</p>
     * <pre>
     * {@code
     * Map<String, String> filters = Map.of(
     *     "status", "active",
     *     "department", "IT"
     * );
     * userRows.resolve(filters).shouldNotBeEmpty();
     * }
     * </pre>
     *
     * @param params map параметров для подстановки
     * @return новая коллекция с подставленными параметрами
     * @throws UnsupportedOperationException если коллекция не имеет шаблона локатора
     */
    public ElementList<T> resolve(Map<String, String> params) {
        if (locatorTemplate == null) {
            throw new UnsupportedOperationException(
                String.format("Метод resolve() доступен только для коллекций с параметризованным локатором. " +
                    "Коллекция '%s' не имеет шаблона локатора.", name));
        }
        
        // Используем LocatorResolver для подстановки параметров
        String resolvedLocator = com.company.hex.ui.locator.LocatorResolver.resolve(locatorTemplate, params);
        
        logger.debug("Resolved локатор для '{}': '{}' -> '{}'", name, locatorTemplate, resolvedLocator);
        
        // Создаем новый resolver с подставленным локатором
        Supplier<ElementsCollection> newResolver = () -> {
            if (isXpath) {
                return com.codeborne.selenide.Selenide.$$x(resolvedLocator);
            } else {
                return com.codeborne.selenide.Selenide.$$(resolvedLocator);
            }
        };
        
        // Создаем новую коллекцию с разрешенным локатором
        return new ElementList<>(name, newResolver, type, context, resolvedLocator, isXpath);
    }
    
    // ==================== Iterable ====================
    
    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }
    
    @Override
    public Spliterator<T> spliterator() {
        return stream().spliterator();
    }
    
    @Override
    public String toString() {
        return String.format("ElementList[name='%s', type=%s, size=%d, context=%s]",
            name, type.getSimpleName(), size(), context);
    }
    
    /**
     * Создать отфильтрованную коллекцию из списка элементов.
     *
     * @param elements список элементов
     * @return новая коллекция
     */
    protected ElementList<T> createFilteredList(List<T> elements) {
        // Создаем resolver который возвращает фиксированный список
        Supplier<ElementsCollection> snapshotResolver = () -> {
            throw new UnsupportedOperationException(
                "Отфильтрованная коллекция работает со снэпшотом и не поддерживает динамическую резолвацию");
        };
        
        return new SnapshotElementList<>(name, elements, type, context);
    }
    
    /**
     * Внутренний класс для коллекций-снэпшотов (работает с фиксированным списком).
     */
    protected static class SnapshotElementList<T extends BaseElement> extends ElementList<T> {
        
        private final List<T> elements;
        
        public SnapshotElementList(String name, List<T> elements, Class<T> type, UiContext context) {
            super(name, () -> {
                throw new UnsupportedOperationException("Snapshot list не поддерживает resolver");
            }, type, context);
            this.elements = new ArrayList<>(elements);
        }
        
        @Override
        public int size() {
            return elements.size();
        }
        
        @Override
        public T get(int index) {
            if (index < 0 || index >= elements.size()) {
                throw new IndexOutOfBoundsException(
                    String.format("Индекс %d вне диапазона для '%s' (размер=%d)",
                        index, getName(), elements.size()));
            }
            return elements.get(index);
        }
        
        @Override
        public Stream<T> stream() {
            return elements.stream();
        }
        
        @Override
        public List<T> snapshot() {
            return new ArrayList<>(elements);
        }
    }
}