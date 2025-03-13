import client.StellarBurgersClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

public class UserRegistrationTest {
    private StellarBurgersClient client;
    private User testUser;
    private String token;

    @Before
    public void setUp() {
        client = new StellarBurgersClient();
    }

    @After
    public void tearDown() {
        if (token != null) {
            client.deleteUser(token);
        }
    }

    @Test
    @Step("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        testUser = User.generateRandomUser();
        Response response = client.createUser(testUser.getEmail(), testUser.getPassword(), testUser.getName());
        response.then().statusCode(200).body("success", equalTo(true));

        token = response.jsonPath().getString("accessToken");
    }

    @Test
    @Step("Создание пользователя, который уже зарегистрирован")
    public void testCreateDuplicateUser() {
        String email = "angrysuper@yandex.ru";
        String password = "password";
        String name = "Username";

        client.createUser(email, password, name);
        Response response = client.createUser(email, password, name);

        response.then().statusCode(403).body("message", equalTo("User already exists"));
    }

    @Test
    @Step("Создание пользователя без email")
    public void testCreateUserWithoutEmail() {
        Response response = client.createUser("", "password", "Username");
        response.then().statusCode(403).body("message", equalTo("Email, password and name are required fields"));
    }


}
