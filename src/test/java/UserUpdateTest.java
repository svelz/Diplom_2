import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserUpdateTest {

    private String authToken;
    private String currentEmail;
    private final String email = "chengepassword@yandex.ru";
    private final String password = "password";

    @Before
    @Step("Авторизация пользователя или регистрация нового")
    public void setUp() {
        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}")
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/auth/login");

        if (loginResponse.getStatusCode() == 200) {
            authToken = loginResponse.jsonPath().getString("accessToken");
        } else if (loginResponse.getStatusCode() == 401) {
            registerNewUser();
            authToken = loginUser().jsonPath().getString("accessToken");
        } else {
            throw new IllegalStateException("Ошибка API при логине! Код: " + loginResponse.getStatusCode());
        }

        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Ошибка: `accessToken` не получен!");
        }

        getCurrentUserInfo();
    }

    @Step("Регистрация нового пользователя")
    private void registerNewUser() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"name\": \"TestUser\"}")
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/auth/register")
                .then()
                .statusCode(200);
    }

    @Step("Логин нового пользователя")
    private Response loginUser() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}")
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/auth/login");
    }

    @Step("Получение текущего email пользователя")
    private void getCurrentUserInfo() {
        Response userInfoResponse = given()
                .header("Authorization", authToken)
                .when()
                .get("https://stellarburgers.nomoreparties.site/api/auth/user");

        userInfoResponse.then().statusCode(200);
        currentEmail = userInfoResponse.jsonPath().getString("user.email");
    }

    @Test
    @Step("Обновление email пользователя с авторизацией")
    public void userEditEmailWithAuthorization() {
        String newEmail = "newemail" + System.currentTimeMillis() + "@yandex.ru";

        if (newEmail.equals(currentEmail)) {
            return;
        }

        Response updateResponse = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + newEmail + "\"}")
                .when()
                .patch("https://stellarburgers.nomoreparties.site/api/auth/user");

        updateResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @Step("Попытка обновления email без авторизации")
    public void testUserEditWithoutAuthorization() {
        String newEmail = "unauthorized" + System.currentTimeMillis() + "@yandex.ru";

        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + newEmail + "\"}")
                .when()
                .patch("https://stellarburgers.nomoreparties.site/api/auth/user");

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
