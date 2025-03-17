import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.junit4.DisplayName;

import static org.hamcrest.Matchers.*;

public class GetUserOrdersTest {

    private StellarBurgersClient client;
    private String token;

    @Before
    @Step("Авторизация пользователя и получение токена")
    public void setUp() {
        client = new StellarBurgersClient();

        // Используем объект User для логина
        User user = new User("moreordertest@yandex.ru", "password", null);
        Response loginResponse = client.loginUser(user);
        loginResponse.then().statusCode(200);

        token = loginResponse.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя")
    @Description("Проверка успешного получения заказов")
    public void testGetUserOrdersWithAuthorization() {
        Response response = client.getUserOrders(token);
        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("orders.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Получение заказов без авторизации")
    @Description("Проверка ошибки при получении заказов без авторизации")
    public void testGetUserOrdersWithoutAuthorization() {
        Response response = client.getUserOrders(null);
        response.then().statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
