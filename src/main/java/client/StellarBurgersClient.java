package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class StellarBurgersClient {
    private static final String BASE_URL = "https://stellarburgers.nomoreparties.site/api/";

    @Step("Создание нового пользователя")
    public Response createUser(String email, String password, String name) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body("{\"email\":\"" + email + "\", \"password\":\"" + password + "\", \"name\":\"" + name + "\"}")
                .post("auth/register");
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String token) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", token)
                .delete("auth/user");
    }

    @Step("Логин пользователя")
    public Response loginUser(String email, String password) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}")
                .post("auth/login");
    }

    @Step("Создание заказа")
    public Response createOrder(String token, String[] ingredients) {
        String requestBody = "{\"ingredients\": " + (ingredients != null ? "[\"" + String.join("\",\"", ingredients) + "\"]" : "[]") + "}";

        RequestSpecification request = given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(requestBody);

        if (token != null && !token.isEmpty()) {
            request.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
        }

        return request.post("orders");
    }

    @Step("Обновление данных пользователя")
    public Response updateUser(String token, Map<String, String> updatedData) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", token != null ? "Bearer " + token : "")
                .header("Content-type", "application/json")
                .body(updatedData)
                .when()
                .patch("auth/user");
    }

    @Step("Обновление access токена с использованием refresh токена")
    public Response refreshAccessToken(String refreshToken) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body("{\"token\":\"" + refreshToken + "\"}")
                .post("auth/token");
    }
}
