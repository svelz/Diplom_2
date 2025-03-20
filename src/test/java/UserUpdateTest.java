import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.User;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

public class UserUpdateTest {
    private StellarBurgersClient client;
    private String authToken;
    private String currentEmail;
    private User testUser;

    @Before
    @Step("Авторизация пользователя или регистрация нового")
    public void setUp() {
        client = new StellarBurgersClient();
        testUser = new User("chengepassword@yandex.ru", "password", "TestUser");

        Response loginResponse = client.loginUser(testUser);
        logResponse("Login", loginResponse);

        if (loginResponse.getStatusCode() == 200) {
            authToken = extractToken(loginResponse);
        } else if (loginResponse.getStatusCode() == 401) {
            client.createUser(testUser);
            Response secondLoginResponse = client.loginUser(testUser);
            logResponse("Second Login", secondLoginResponse);
            authToken = extractToken(secondLoginResponse);
        } else {
            throw new IllegalStateException("Ошибка API при логине! Код: " + loginResponse.getStatusCode());
        }

        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Ошибка: `accessToken` не получен!");
        }

        getCurrentUserInfo();
    }

    @Step("Получение текущего email пользователя")
    private void getCurrentUserInfo() {
        Response userInfoResponse = client.getUserOrders(authToken);
        logResponse("Get User Info", userInfoResponse);

        userInfoResponse.then().statusCode(200);
        currentEmail = userInfoResponse.jsonPath().getString("user.email");
    }

    @Test
    @Step("Обновление email пользователя с авторизацией")
    @DisplayName("Обновление email авторизованного пользователя")
    @Description("Тест проверяет, что авторизованный пользователь может обновить email")
    public void userEditEmailWithAuthorization() {
        String newEmail = "newemail" + System.currentTimeMillis() + "@yandex.ru";

        System.out.println("Текущий токен перед обновлением: " + authToken);
        authToken = cleanToken(authToken);  // Очистка токена

        if (newEmail.equals(currentEmail)) {
            return; // Если email не изменился, тест можно пропустить
        }

        Map<String, String> updateData = new HashMap<>();
        updateData.put("email", newEmail);
        System.out.println("Тело запроса на обновление: " + updateData);

        Response updateResponse = client.updateUser(authToken, updateData);
        logResponse("Update Email", updateResponse);

        if (updateResponse.getStatusCode() == 403) {
            System.out.println("Ошибка 403: Переполучение токена и повторная попытка обновления...");
            authToken = extractToken(client.loginUser(testUser));
            updateResponse = client.updateUser(authToken, updateData);
            logResponse("Retry Update", updateResponse);
        }

        updateResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @Step("Попытка обновления email без авторизации")
    @DisplayName("Попытка обновления email без авторизации")
    @Description("Тест проверяет, что без авторизации обновить email невозможно")
    public void testUserEditWithoutAuthorization() {
        String newEmail = "unauthorized" + System.currentTimeMillis() + "@yandex.ru";

        Map<String, String> updateData = new HashMap<>();
        updateData.put("email", newEmail);

        Response response = client.updateUser(null, updateData);
        logResponse("Unauthorized Update", response);

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    // Очистка токена от "Bearer "
    private String cleanToken(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        return token.trim().startsWith("Bearer ") ? token.trim().substring(7) : token.trim();
    }

    // Извлечение токена из ответа
    private String extractToken(Response response) {
        return cleanToken(response.jsonPath().getString("accessToken"));
    }

    // Логирование ответов сервера
    private void logResponse(String action, Response response) {
        System.out.println(action + " Response Code: " + response.getStatusCode());
        System.out.println(action + " Response Body: " + response.getBody().asString());
    }
}
