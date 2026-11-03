package tests;

import data.TestData;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

public class AccountTest extends BaseTest {

    private Map<String, String> account;

    @BeforeMethod(alwaysRun = true)
    public void createTestAccount() {
        account = TestData.newAccount();
        api.createAccount(account)
                .then()
                .body("responseCode", equalTo(201));
    }

    @AfterMethod(alwaysRun = true)
    public void deleteTestAccount() {
        if (account != null) {
            api.deleteAccount(account.get("email"), account.get("password"));
        }
    }

    @Test(groups = {"smoke", "regression"})
    public void verifyLoginWithValidDetails() {
        api.verifyLogin(account.get("email"), account.get("password"))
                .then()
                .statusCode(200)
                .body("responseCode", equalTo(200))
                .body("message", equalTo("User exists!"));
    }

    @Test(groups = {"regression"})
    public void createAccountWithExistingEmailIsRejected() {
        api.createAccount(account)
                .then()
                .statusCode(200)
                .body("responseCode", equalTo(400))
                .body("message", equalTo("Email already exists!"));
    }

    @Test(groups = {"regression"})
    public void verifyLoginWithWrongPasswordIsRejected() {
        String wrongPassword = account.get("password") + "-wrong";

        api.verifyLogin(account.get("email"), wrongPassword)
                .then()
                .statusCode(200)
                .body("responseCode", equalTo(404))
                .body("message", equalTo("User not found!"));
    }

    @Test(groups = {"regression"})
    public void deletedAccountCanNoLongerLogIn() {
        api.deleteAccount(account.get("email"), account.get("password"))
                .then()
                .statusCode(200)
                .body("responseCode", equalTo(200))
                .body("message", equalTo("Account deleted!"));

        api.verifyLogin(account.get("email"), account.get("password"))
                .then()
                .statusCode(200)
                .body("responseCode", equalTo(404))
                .body("message", equalTo("User not found!"));
    }


}
