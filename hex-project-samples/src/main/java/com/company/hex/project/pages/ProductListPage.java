package com.company.hex.project.pages;

import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Elements;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.collections.ElementList;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.Input;
import com.company.hex.ui.elements.TextElement;
import io.qameta.allure.Step;

import java.util.List;

import static com.codeborne.selenide.Condition.visible;

/**
 * Пример страницы со списком продуктов для демонстрации работы с ElementList.
 * Показывает различные способы работы с коллекциями элементов.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Page(url = "/products", title = "Product List Page")
public class ProductListPage extends BasePage {
    
    // ==================== Элементы страницы ====================
    
    @Element(name = "Search Input", xpath = "//input[@id='search']")
    Input searchInput;
    
    @Element(name = "Search Button", xpath = "//button[@id='search-btn']")
    Button searchButton;
    
    @Element(name = "Results Count", xpath = "//span[@class='results-count']")
    TextElement resultsCount;
    
    // ==================== Коллекции элементов ====================
    
    /**
     * Список всех карточек продуктов на странице.
     */
    @Elements(name = "Product Cards", xpath = "//div[@class='product-card']")
    ElementList<Button> productCards;
    
    /**
     * Список названий продуктов.
     */
    @Elements(name = "Product Names", xpath = "//div[@class='product-card']//h3[@class='product-name']")
    ElementList<TextElement> productNames;
    
    /**
     * Список цен продуктов.
     */
    @Elements(name = "Product Prices", xpath = "//div[@class='product-card']//span[@class='price']")
    ElementList<TextElement> productPrices;
    
    /**
     * Список кнопок "Добавить в корзину".
     */
    @Elements(name = "Add to Cart Buttons", xpath = "//button[@class='add-to-cart']")
    ElementList<Button> addToCartButtons;
    
    /**
     * Список элементов пагинации.
     */
    @Elements(name = "Pagination Items", xpath = "//ul[@class='pagination']//li")
    ElementList<Button> paginationItems;
    
    // ==================== Бизнес-методы ====================
    
    /**
     * Поиск продуктов по запросу.
     * 
     * @param query поисковый запрос
     * @return this для fluent API
     */
    @Step("Поиск продуктов: '{query}'")
    public ProductListPage searchProducts(String query) {
        searchInput.fill(query);
        searchButton.click();
        return this;
    }
    
    /**
     * Получить количество найденных продуктов.
     * 
     * @return количество продуктов
     */
    @Step("Получить количество продуктов")
    public int getProductCount() {
        return productCards.size();
    }
    
    /**
     * Проверить, что отображается указанное количество продуктов.
     * 
     * @param expectedCount ожидаемое количество
     * @return this для fluent API
     */
    @Step("Проверить количество продуктов: {expectedCount}")
    public ProductListPage shouldHaveProductCount(int expectedCount) {
        productCards.shouldHaveSize(expectedCount);
        return this;
    }
    
    /**
     * Проверить, что список продуктов не пуст.
     * 
     * @return this для fluent API
     */
    @Step("Проверить что список продуктов не пуст")
    public ProductListPage shouldHaveProducts() {
        productCards.shouldNotBeEmpty();
        return this;
    }
    
    /**
     * Получить все названия продуктов.
     * 
     * @return список названий
     */
    @Step("Получить все названия продуктов")
    public List<String> getAllProductNames() {
        return productNames.texts();
    }
    
    /**
     * Получить все цены продуктов.
     * 
     * @return список цен
     */
    @Step("Получить все цены продуктов")
    public List<String> getAllProductPrices() {
        return productPrices.texts();
    }
    
    /**
     * Найти продукт по названию.
     * 
     * @param productName название продукта
     * @return индекс продукта или -1 если не найден
     */
    @Step("Найти продукт: '{productName}'")
    public int findProductByName(String productName) {
        List<String> names = getAllProductNames();
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).contains(productName)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Кликнуть по продукту с указанным индексом.
     * 
     * @param index индекс продукта (0-based)
     * @return this для fluent API
     */
    @Step("Кликнуть по продукту с индексом {index}")
    public ProductListPage clickProduct(int index) {
        productCards.get(index).click();
        return this;
    }
    
    /**
     * Кликнуть по первому продукту.
     * 
     * @return this для fluent API
     */
    @Step("Кликнуть по первому продукту")
    public ProductListPage clickFirstProduct() {
        productCards.first().click();
        return this;
    }
    
    /**
     * Кликнуть по последнему продукту.
     * 
     * @return this для fluent API
     */
    @Step("Кликнуть по последнему продукту")
    public ProductListPage clickLastProduct() {
        productCards.last().click();
        return this;
    }
    
    /**
     * Добавить продукт в корзину по индексу.
     * 
     * @param index индекс продукта (0-based)
     * @return this для fluent API
     */
    @Step("Добавить в корзину продукт с индексом {index}")
    public ProductListPage addToCart(int index) {
        addToCartButtons.get(index).click();
        return this;
    }
    
    /**
     * Добавить все видимые продукты в корзину.
     * 
     * @return this для fluent API
     */
    @Step("Добавить все продукты в корзину")
    public ProductListPage addAllToCart() {
        addToCartButtons.forEachElement(button -> {
            button.shouldBe(visible);
            button.click();
        });
        return this;
    }
    
    /**
     * Фильтровать продукты по названию (используя Java предикат).
     * 
     * @param namePattern паттерн для поиска в названии
     * @return отфильтрованный список названий
     */
    @Step("Фильтровать продукты по названию: '{namePattern}'")
    public List<String> filterProductsByName(String namePattern) {
        return productNames
            .filter(name -> name.getText().contains(namePattern))
            .map(TextElement::getText)
            .toList();
    }
    
    /**
     * Получить продукты в указанном ценовом диапазоне.
     * 
     * @param minPrice минимальная цена
     * @param maxPrice максимальная цена
     * @return список названий продуктов в диапазоне
     */
    @Step("Получить продукты в диапазоне цен: {minPrice} - {maxPrice}")
    public List<String> getProductsInPriceRange(double minPrice, double maxPrice) {
        List<String> names = getAllProductNames();
        List<String> prices = getAllProductPrices();
        
        List<String> result = new java.util.ArrayList<>();
        for (int i = 0; i < names.size() && i < prices.size(); i++) {
            String priceStr = prices.get(i).replaceAll("[^0-9.]", "");
            try {
                double price = Double.parseDouble(priceStr);
                if (price >= minPrice && price <= maxPrice) {
                    result.add(names.get(i));
                }
            } catch (NumberFormatException e) {
                // Пропускаем некорректные цены
            }
        }
        
        return result;
    }
    
    /**
     * Перейти на указанную страницу пагинации.
     * 
     * @param pageNumber номер страницы
     * @return this для fluent API
     */
    @Step("Перейти на страницу {pageNumber}")
    public ProductListPage goToPage(int pageNumber) {
        paginationItems
            .filter(item -> item.getText().equals(String.valueOf(pageNumber)))
            .findFirst(item -> true)  // Получаем первый элемент из отфильтрованного списка
            .ifPresent(Button::click);
        return this;
    }
    
    /**
     * Проверить, что все продукты видимы.
     * 
     * @return this для fluent API
     */
    @Step("Проверить что все продукты видимы")
    public ProductListPage shouldAllProductsBeVisible() {
        productCards.forEachElement(card -> card.shouldBe(visible));
        return this;
    }
}