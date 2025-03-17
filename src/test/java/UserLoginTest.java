import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserLoginTest {
    private StellarBurgersClient client;
    private User testUser;
    private String token;

    @Before
    @Step("Создание тестового пользователя")
    public void setUp() {
        client = new StellarBurgersClient();
        testUser = User.generateRandomUser(); // Генерируем случайного пользователя

        Response response = client.createUser(testUser); // Передаем объект User
        response.then().statusCode(200);

        token = response.jsonPath().getString("accessToken");
    }

    @After
    @Step("Удаление тестового пользователя после выполнения тестов")
    public void tearDown() {
        if (token != null) {
            client.deleteUser(token);
        }
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    @Description("Тест успешной авторизации пользователя")
    public void testLoginWithValidUser() {
        Response response = client.loginUser(testUser); // Логинимся через объект User
        response.then().statusCode(200).body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Логин с неверными учетными данными")
    @Description("Тест авторизации с неправильными данными")
    public void testLoginWithInvalidCredentials() {
        User invalidUser = new User("wrong@mail.com", "wrongpassword", null); // Создаем некорректного пользователя
        Response response = client.loginUser(invalidUser);
        response.then().statusCode(401).body("message", equalTo("email or password are incorrect"));
    }
}
