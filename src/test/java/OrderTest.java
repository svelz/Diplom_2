import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.Order;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

public class OrderTest {

    private StellarBurgersClient client;
    private User user;
    private String token;
    private String refreshToken;

    @Before
    @Step("Создание пользователя и авторизация")
    public void setUp() {
        client = new StellarBurgersClient();
        user = User.generateRandomUser(); // Создаем объект пользователя

        Response response = client.createUser(user); // Передаем объект User
        response.then().statusCode(200);

        Response loginResponse = client.loginUser(user); // Логинимся тоже через объект User
        loginResponse.then().statusCode(200);

        token = loginResponse.jsonPath().getString("accessToken");
        refreshToken = loginResponse.jsonPath().getString("refreshToken");
    }

    @After
    @Step("Удаление пользователя после теста")
    public void tearDown() {
        if (token != null) {
            client.deleteUser(token);
        }
    }

    @Step("Обновление токена доступа")
    private void refreshAccessTokenIfNeeded() {
        Response refreshResponse = client.refreshAccessToken(refreshToken);
        if (refreshResponse.getStatusCode() == 200) {
            token = refreshResponse.jsonPath().getString("accessToken");
        }
    }
    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Тест успешного создания заказа авторизованным пользователем")
    public void testCreateOrderWithAuthorization() {
        refreshAccessTokenIfNeeded();
        Response ingredientsResponse = client.getIngredients();
        ingredientsResponse.then().statusCode(200);
        List<String> ingredientIds = ingredientsResponse.jsonPath().getList("data._id");
        if (ingredientIds.isEmpty()) {
            throw new RuntimeException("Не удалось получить список ингредиентов");
        }
        Order order = new Order(new String[]{ingredientIds.get(0), ingredientIds.get(1)}); // Используем два первых ингредиента
        Response response = client.createOrder(token, order);
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Тест успешного создания заказа без авторизации")
    public void testCreateOrderWithoutAuthorization() {
        Response ingredientsResponse = client.getIngredients();
        ingredientsResponse.then().statusCode(200);
        List<String> ingredientIds = ingredientsResponse.jsonPath().getList("data._id");
        if (ingredientIds.isEmpty()) {
            throw new RuntimeException("Не удалось получить список ингредиентов");
        }
        Order order = new Order(new String[]{ingredientIds.get(0), ingredientIds.get(1)});
        Response response = client.createOrder(null, order);
        response.then().body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Тест проверяет, что заказ без ингредиентов невозможен")
    public void testCreateOrderWithoutIngredients() {
        refreshAccessTokenIfNeeded();
        Order order = new Order(new String[]{});

        Response response = client.createOrder(token, order);

        response.then().statusCode(400)
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Тест проверяет, что заказ с неправильным хешем ингредиентов невозможен")
    public void testCreateOrderWithInvalidIngredientHash() {
        refreshAccessTokenIfNeeded();
        Order order = new Order(new String[]{"invalid_hash"});

        Response response = client.createOrder(token, order);
        response.then().statusCode(400)
                .body("message", equalTo("One or more ids provided are incorrect"));
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя")
    @Description("Тест проверяет получение списка заказов авторизованным пользователем")
    public void testGetOrdersWithAuthorization() {
        refreshAccessTokenIfNeeded();
        Response response = client.getUserOrders(token);
        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение заказов без авторизации")
    @Description("Тест проверяет, что без авторизации получить заказы невозможно")
    public void testGetOrdersWithoutAuthorization() {
        Response response = client.getUserOrders(null);
        response.then().statusCode(401)
                .body("message", equalTo("You should be authorised"));
    }
}
