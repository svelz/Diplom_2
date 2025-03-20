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

public class UserRegistrationTest {
    private StellarBurgersClient client;
    private User testUser;
    private String token;

    @Before
    @Step("Создание тестового клиента")
    public void setUp() {
        client = new StellarBurgersClient();
        testUser = User.generateRandomUser();
    }

    @After
    @Step("Удаление пользователя после теста, если был создан")
    public void tearDown() {
        if (token != null) {
            client.deleteUser(token);
        }
    }

    @Test
    @DisplayName("Регистрация нового пользователя")
    @Description("Тест успешного создания нового пользователя")
    public void testCreateNewUser() {
        Response response = client.createUser(testUser); // Передаем объект User
        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());
        token = response.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Регистрации уже существующего пользователя")
    @Description("Тест попытки регистрации дублирующего пользователя")
    public void testCreateDuplicateUser() {
        Response response1 = client.createUser(testUser);
        response1.then().statusCode(200);

        Response response2 = client.createUser(testUser);
        response2.then().statusCode(403)
                .body("message", equalTo("User already exists"));

        token = response1.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Создание пользователя без email")
    @Description("Тест на ошибку при регистрацию пользователя без email")
    public void testCreateUserWithoutEmail() {
        User userWithoutEmail = new User(null, "password123", "TestUser");
        Response response = client.createUser(userWithoutEmail);

        response.then().statusCode(403)
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Тест на ошибку регистрацию пользователя без пароля")
    public void testCreateUserWithoutPassword() {
        User userWithoutPassword = new User("testuser@example.com", null, "TestUser");
        Response response = client.createUser(userWithoutPassword);

        response.then().statusCode(403)
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    @Description("Тест на ошибку регистрацию пользователя без имени")
    public void testCreateUserWithoutName() {
        User userWithoutName = new User("testuser@example.com", "password123", null);
        Response response = client.createUser(userWithoutName);

        response.then().statusCode(403) // Исправлено с 400 на 403
                .body("message", equalTo("Email, password and name are required fields"));
    }
}
