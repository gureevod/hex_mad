# ElementList - Работа с коллекциями элементов

Этот документ описывает использование `ElementList<T>` для работы с коллекциями UI элементов в Hex Framework.

## Оглавление

1. [Обзор](#обзор)
2. [Базовое использование](#базовое-использование)
3. [Методы доступа](#методы-доступа)
4. [Фильтрация и поиск](#фильтрация-и-поиск)
5. [Трансформации](#трансформации)
6. [Assertions](#assertions)
7. [Примеры из реальных проектов](#примеры-из-реальных-проектов)

---

## Обзор

`ElementList<T>` - это типобезопасная коллекция UI элементов с "живой" резолвацией и функциональным API.

### Ключевые особенности

- ✅ **Ленивая резолвация** - всегда отражает текущее состояние DOM
- ✅ **Типобезопасность** - через дженерики
- ✅ **Функциональный API** - filter, map, forEach
- ✅ **Автоматические Allure steps** - с агрегацией
- ✅ **Контекстное логирование** - page + component + collection
- ✅ **Поддержка Selenide условий** - и Java предикатов

---

## Базовое использование

### Объявление через аннотацию

```java
@Page(url = "/products", title = "Product List Page")
public class ProductListPage extends BasePage {
    
    // Коллекция кнопок
    @Elements(name = "Product Cards", xpath = "//div[@class='product-card']")
    ElementList<Button> productCards;
    
    // Коллекция текстовых элементов
    @Elements(name = "Product Names", xpath = "//h3[@class='product-name']")
    ElementList<TextElement> productNames;
    
    // Коллекция чекбоксов
    @Elements(name = "Filters", xpath = "//input[@type='checkbox']")
    ElementList<Checkbox> filters;
}
```

---

## Методы доступа

### Получение размера

```java
// Получить количество элементов
int count = productCards.size();

// Проверить пустоту
boolean isEmpty = productCards.isEmpty();
```

### Доступ по индексу (0-based)

```java
// Получить элемент по индексу
Button firstCard = productCards.get(0);
Button secondCard = productCards.nth(1);  // синоним для get()

// Первый и последний элементы
Button first = productCards.first();
Button last = productCards.last();

// Единственный элемент (выбросит исключение если != 1)
Button single = productCards.single();
```

### Обработка ошибок

```java
// IndexOutOfBoundsException с подробным сообщением
try {
    Button card = productCards.get(100);
} catch (IndexOutOfBoundsException e) {
    // "Индекс 100 вне диапазона для 'Product Cards' (размер=10) 
    //  в странице 'Product List Page' компоненте 'Root'"
}

// IllegalStateException для single()
try {
    Button card = productCards.single();
} catch (IllegalStateException e) {
    // "Ожидался ровно один элемент в 'Product Cards', но найдено 10"
}
```

---

## Фильтрация и поиск

### Фильтрация по Selenide условию (с ожиданием)

```java
import static com.codeborne.selenide.Condition.*;

// Отфильтровать видимые элементы
ElementList<Button> visibleCards = productCards.filterBy(visible);

// Отфильтровать по тексту
ElementList<TextElement> premiumProducts = productNames.filterBy(matchText("Premium"));

// Цепочка фильтров
ElementList<Button> result = productCards
    .filterBy(visible)
    .filterBy(enabled);
```

### Поиск по Selenide условию

```java
// Найти первый элемент
Optional<Button> found = productCards.findBy(matchText("Premium"));

found.ifPresent(Button::click);

// Или с обработкой отсутствия
Button card = productCards.findBy(visible)
    .orElseThrow(() -> new AssertionError("Не найдено видимых карточек"));
```

### Фильтрация по Java предикату (работает по снэпшоту)

```java
// Фильтрация с кастомной логикой
ElementList<TextElement> filtered = productNames.filter(name -> {
    String text = name.getText();
    return text.startsWith("Premium") && text.length() > 10;
});

// Поиск первого
Optional<TextElement> first = productNames.findFirst(name -> 
    name.getText().contains("Special")
);
```

---

## Трансформации

### Stream API

```java
// Получить Stream элементов
Stream<Button> stream = productCards.stream();

// Map и collect
List<String> names = productNames
    .map(TextElement::getText)
    .collect(Collectors.toList());

// Filter и map
List<String> premiumNames = productNames
    .stream()
    .filter(name -> name.getText().contains("Premium"))
    .map(TextElement::getText)
    .collect(Collectors.toList());
```

### Утилиты для быстрого извлечения

```java
// Получить все тексты
List<String> texts = productNames.texts();

// Получить значения атрибута
List<String> ids = productCards.attributes("data-id");

// Получить значения (value)
List<String> values = inputs.values();  // сахар для attributes("value")
```

### Итерация

```java
// forEach с действием
productCards.forEachElement(card -> {
    card.shouldBe(visible);
    card.click();
});

// Стандартный for-each (Iterable)
for (Button card : productCards) {
    card.shouldBe(visible);
}
```

---

## Assertions

### Проверка размера

```java
// Точный размер
productCards.shouldHaveSize(10);

// Пустота
productCards.shouldBeEmpty();
productCards.shouldNotBeEmpty();
```

### Selenide условия для коллекций

```java
import static com.codeborne.selenide.CollectionCondition.*;

// Использование стандартных Selenide условий
productCards.shouldHave(size(10));
productCards.shouldHave(sizeGreaterThan(5));
productCards.shouldHave(sizeLessThan(20));

// Проверка текстов
productNames.shouldHave(texts("Product 1", "Product 2", "Product 3"));
```

### Fluent assertions

```java
// Цепочка проверок
productCards
    .shouldNotBeEmpty()
    .shouldHaveSize(10)
    .filterBy(visible)
    .shouldHaveSize(10);
```

---

## Примеры из реальных проектов

### Пример 1: Поиск и клик по продукту

```java
@Step("Выбрать продукт: '{productName}'")
public ProductDetailsPage selectProduct(String productName) {
    productNames
        .filter(name -> name.getText().contains(productName))
        .findFirst(name -> true)
        .ifPresent(name -> {
            int index = productNames.snapshot().indexOf(name);
            productCards.get(index).click();
        });
    
    return new ProductDetailsPage();
}
```

### Пример 2: Фильтрация по цене

```java
@Step("Получить продукты дешевле {maxPrice}")
public List<String> getProductsCheaperThan(double maxPrice) {
    List<String> names = productNames.texts();
    List<String> prices = productPrices.texts();
    
    List<String> result = new ArrayList<>();
    for (int i = 0; i < names.size() && i < prices.size(); i++) {
        double price = parsePrice(prices.get(i));
        if (price < maxPrice) {
            result.add(names.get(i));
        }
    }
    
    return result;
}
```

### Пример 3: Массовые операции

```java
@Step("Добавить все продукты в корзину")
public ProductListPage addAllToCart() {
    addToCartButtons.forEachElement(button -> {
        button.shouldBe(visible);
        button.click();
        // Ждем анимации
        sleep(500);
    });
    return this;
}
```

### Пример 4: Проверка сортировки

```java
@Step("Проверить что продукты отсортированы по цене")
public ProductListPage shouldBeSortedByPrice() {
    List<String> priceTexts = productPrices.texts();
    List<Double> prices = priceTexts.stream()
        .map(this::parsePrice)
        .collect(Collectors.toList());
    
    List<Double> sortedPrices = new ArrayList<>(prices);
    Collections.sort(sortedPrices);
    
    if (!prices.equals(sortedPrices)) {
        throw new AssertionError("Продукты не отсортированы по цене");
    }
    
    return this;
}
```

### Пример 5: Работа с пагинацией

```java
@Step("Собрать все продукты со всех страниц")
public List<String> getAllProductsFromAllPages() {
    List<String> allProducts = new ArrayList<>();
    
    do {
        allProducts.addAll(productNames.texts());
        
        Optional<Button> nextButton = paginationItems
            .findFirst(item -> item.getText().equals("Next"));
        
        if (nextButton.isEmpty() || !nextButton.get().isDisplayed()) {
            break;
        }
        
        nextButton.get().click();
        productCards.first().shouldBe(visible);  // Ждем загрузки
        
    } while (true);
    
    return allProducts;
}
```

---

## Лучшие практики

### 1. Используйте осмысленные имена

```java
// ✅ Хорошо
@Elements(name = "Product Cards", xpath = "//div[@class='product-card']")
ElementList<Button> productCards;

// ❌ Плохо
@Elements(name = "Items", xpath = "//div[@class='product-card']")
ElementList<Button> items;
```

### 2. Выбирайте правильный метод фильтрации

```java
// Для ожидания появления элементов - используйте filterBy (Selenide)
ElementList<Button> visible = cards.filterBy(visible);

// Для сложной логики - используйте filter (Java предикат)
ElementList<Button> complex = cards.filter(card -> {
    String text = card.getText();
    String id = card.getAttribute("data-id");
    return text.contains("Premium") && Integer.parseInt(id) > 100;
});
```

### 3. Материализуйте при необходимости

```java
// Если нужен фиксированный снэпшот
List<Button> snapshot = productCards.snapshot();

// Для работы с индексами
List<String> names = productNames.texts();
for (int i = 0; i < names.size(); i++) {
    if (names.get(i).contains("Special")) {
        productCards.get(i).click();
        break;
    }
}
```

### 4. Обрабатывайте пустые коллекции

```java
// Проверка перед использованием
if (!productCards.isEmpty()) {
    productCards.first().click();
}

// Или используйте Optional
productCards.findFirst(card -> card.isDisplayed())
    .ifPresent(Button::click);
```

---

## Логирование и Allure

Все операции с `ElementList` автоматически логируются и создают Allure steps:

```java
// Код
productCards.shouldHaveSize(10);

// Allure step
└─ 'Product Cards' должна иметь размер 10

// Лог
INFO  - Проверка размера 'Product Cards' = 10 в странице 'Product List Page' компоненте 'Root'
```

```java
// Код
productCards.forEachElement(Button::click);

// Allure step
└─ Выполнить действие для каждого элемента 'Product Cards'

// Логи
INFO  - Выполнение действия для каждого элемента 'Product Cards' в странице 'Product List Page' компоненте 'Root'
DEBUG - Обработка 10 элементов из 'Product Cards'
TRACE - Обработка элемента [0] из 'Product Cards'
TRACE - Обработка элемента [1] из 'Product Cards'
...
```

---

## Заключение

`ElementList<T>` предоставляет мощный и удобный API для работы с коллекциями элементов:

- 🎯 Минимум boilerplate кода
- 🔍 Гибкая фильтрация и поиск
- 📊 Автоматическое логирование
- ✅ Типобезопасность
- 🚀 Производительность

Используйте `ElementList` для всех случаев работы с множественными элементами на странице!