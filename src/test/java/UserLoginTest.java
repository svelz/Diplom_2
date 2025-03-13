import client.StellarBurgersClient;
import io.qameta.allure.Step;
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
    public void setUp() {
        client = new StellarBurgersClient();
        testUser = User.generateRandomUser();
        Response response = client.createUser(testUser.getEmail(), testUser.getPassword(), testUser.getName());
        token = response.jsonPath().getString("accessToken");
        System.out.println("Generated Token: " + token); // <-- Логирование для отладки

    }

    @After
    public void tearDown() {
        if (token != null) {
            client.deleteUser(token);
        }
    }

    @Test
    @Step("Логин под существующим пользователем")
    public void testLoginWithValidUser() {
        Response response = client.loginUser(testUser.getEmail(), testUser.getPassword());
        response.then().statusCode(200).body("accessToken", notNullValue());
    }

    @Test
    @Step("Логин с неверными данными")
    public void testLoginWithInvalidCredentials() {
        Response response = client.loginUser("wrong@mail.com", "wrongpassword");
        response.then().statusCode(401).body("message", equalTo("email or password are incorrect"));
    }

}
