package tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;

public class LoginTest extends BaseTest {

    @DataProvider(name = "missingLoginFields")
    public Object[][] missingLoginFields() {
        return new Object[][]{
                {null, null},
                {"test@example.com", null},
                {null, "password"}
        };
    }

    @Test(dataProvider = "missingLoginFields", groups = {"regression"})
    public void verifyLoginWithMissingFieldIsBadRequest(String email, String password) {
        // This API reports errors in the JSON body, not in the HTTP status
        api.verifyLogin(email, password)
                .then()
                .statusCode(200)
                .body("responseCode", equalTo(400))
                .body("message", equalTo("Bad request, email or password parameter is missing in POST request."));
    }
}