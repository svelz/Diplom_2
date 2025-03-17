package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import model.Order;
import model.User;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class StellarBurgersClient {
    private static final String BASE_URL = "https://stellarburgers.nomoreparties.site/api/";

    @Step("Создание нового пользователя")
    public Response createUser(User user) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(user) // Используем объект User вместо отдельных строк
                .post("auth/register");
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String token) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", token != null ? "Bearer " + token : "")
                .delete("auth/user");
    }

    @Step("Логин пользователя")
    public Response loginUser(User user) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(user) // Передаем объект User
                .post("auth/login");
    }

    @Step("Создание заказа")
    public Response createOrder(String token, Order order) {
        RequestSpecification request = given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(order); // Используем объект Order

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
                .patch("auth/user");
    }

    @Step("Обновление access токена с использованием refresh токена")
    public Response refreshAccessToken(String refreshToken) {
        Map<String, String> requestBody = Map.of("token", refreshToken);

        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .body(requestBody) // Передаем Map вместо String
                .post("auth/token");
    }

    @Step("Получение заказов пользователя")
    public Response getUserOrders(String token) {
        RequestSpecification request = given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json");

        if (token != null && !token.isEmpty()) {
            request.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
        }

        return request.get("orders");
    }

    @Step("Получение списка валидных ингредиентов")
    public String[] getValidIngredientIds() {
        Response response = given()
                .baseUri(BASE_URL)
                .get(Endpoints.ORDERS);

        response.then().statusCode(200);
        return response.jsonPath().getList("data._id", String.class).toArray(new String[0]);
    }

    @Step("Получение списка ингредиентов")
    public Response getIngredients() {
        return given()
                .baseUri(BASE_URL)
                .get("ingredients");
    }

}
