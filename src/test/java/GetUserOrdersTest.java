import client.StellarBurgersClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetUserOrdersTest {

    private StellarBurgersClient client;
    private String token;

    @Before
    @Step("Авторизация пользователя и получение токена")
    public void setUp() {
        client = new StellarBurgersClient();
        Response loginResponse = client.loginUser("moreordertest@yandex.ru", "password");
        loginResponse.then().statusCode(200);
        token = loginResponse.jsonPath().getString("accessToken").replace("Bearer ", "");
    }

    @Test
    @Step("Получение заказов авторизованного пользователя")
    public void testGetUserOrdersWithAuthorization() {
        Response response = given()
                .baseUri("https://stellarburgers.nomoreparties.site/api")
                .header("Authorization", "Bearer " + token)
                .get("/orders");

        response.prettyPrint();

        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("orders.size()", greaterThan(0));
    }

    @Test
    @Step("Получение заказов без авторизации")
    public void testGetUserOrdersWithoutAuthorization() {
        Response response = given()
                .baseUri("https://stellarburgers.nomoreparties.site/api")
                .get("/orders");

        response.prettyPrint();

        response.then().statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
