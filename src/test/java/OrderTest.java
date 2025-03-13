import client.StellarBurgersClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderTest {

    private StellarBurgersClient client;
    private User user;
    private String token;
    private String refreshToken;

    @Before
    @Step("Настройка теста: создание пользователя и авторизация")
    public void setUp() {
        client = new StellarBurgersClient();
        user = User.generateRandomUser();
        Response response = client.createUser(user.getEmail(), user.getPassword(), user.getName());
        response.then().statusCode(200);

        Response loginResponse = client.loginUser(user.getEmail(), user.getPassword());
        token = formatToken(loginResponse.jsonPath().getString("accessToken"));
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
            token = formatToken(refreshResponse.jsonPath().getString("accessToken"));
        }
    }

    @Step("Форматирование токена")
    private String formatToken(String rawToken) {
        if (rawToken == null || rawToken.isEmpty()) {
            throw new IllegalArgumentException("Received an empty token!");
        }
        return rawToken.startsWith("Bearer ") ? rawToken : "Bearer " + rawToken;
    }

    @Test
    @Step("Создание заказа с авторизацией")
    public void testCreateOrderWithAuthorization() {
        refreshAccessTokenIfNeeded();
        String[] ingredients = {"61c0c5a71d1f82001bdaaa72", "609646e4dc916e00276b2870"};
        Response response = client.createOrder(token, ingredients);
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @Step("Создание заказа без авторизации")
    public void testCreateOrderWithoutAuthorization() {
        String ingredients = "[\"61c0c5a71d1f82001bdaaa72\", \"609646e4dc916e00276b2870\"]";
        Response response = given()
                .contentType("application/json")
                .body("{\"ingredients\": " + ingredients + "}")
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/orders");

        response.then()
                .body("success", notNullValue())
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @Step("Создание заказа без ингредиентов")
    public void testCreateOrderWithoutIngredients() {
        refreshAccessTokenIfNeeded();
        Response response = given()
                .baseUri("https://stellarburgers.nomoreparties.site/api")
                .contentType("application/json")
                .header("Authorization", token)
                .body("{\"ingredients\": []}")
                .when()
                .post("/orders");

        response.then()
                .statusCode(400)
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @Step("Создание заказа с неверным хешем ингредиентов")
    public void testCreateOrderWithInvalidIngredientHash() {
        refreshAccessTokenIfNeeded();
        Response response = client.createOrder(token, new String[]{"invalid_hash"});
        response.then().statusCode(400)
                .body("message", equalTo("One or more ids provided are incorrect"));
    }

    @Test
    @Step("Получение заказов конкретного авторизованного пользователя")
    public void testGetOrdersWithAuthorization() {
        refreshAccessTokenIfNeeded();
        Response response = given()
                .header("Authorization", token)
                .get("https://stellarburgers.nomoreparties.site/api/orders");

        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());
    }

    @Test
    @Step("Получение заказов без авторизации")
    public void testGetOrdersWithoutAuthorization() {
        Response response = given()
                .get("https://stellarburgers.nomoreparties.site/api/orders");

        response.then().statusCode(401)
                .body("message", equalTo("You should be authorised"));
    }
}
