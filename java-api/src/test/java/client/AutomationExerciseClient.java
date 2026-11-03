package client;

import config.Config;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;

public class AutomationExerciseClient {

    // automationexercise.com is a shared demo site that occasionally answers 5xx.
    // Retry those briefly; a real failure still shows up after the retries.
    private static final Set<Integer> RETRY_STATUSES = Set.of(502, 503, 504);
    private static final int MAX_RETRIES = 2;

    private final RequestSpecification spec;

    public AutomationExerciseClient() {
        // This API sends JSON but labels it "text/html", so parse text/html as JSON
        RestAssured.registerParser("text/html", Parser.JSON);

        spec = new RequestSpecBuilder()
                .setBaseUri(Config.BASE_URI)
                .setBasePath(Config.BASE_PATH)
                .addFilter(new AllureRestAssured())
                .build();
    }

    public Response getProducts() {
        return withRetry(() -> given(spec).get("/productsList"));
    }

    public Response postProducts() {
        return withRetry(() -> given(spec).post("/productsList"));
    }

    public Response searchProducts(String term){
        return withRetry(() -> {
            RequestSpecification request = given(spec);
            if (term != null) {
                request.formParam("search_product", term);
            }
            return request.post("/searchProduct");
        });
    }

    public Response verifyLogin(String email, String password){
        return withRetry(() -> {
            RequestSpecification request = given(spec);
            if (email != null) {
                request.formParam("email", email);
            }
            if (password != null) {
                request.formParam("password", password);
            }
            return request.post("/verifyLogin");
        });
    }

    public Response createAccount(Map<String, String> account) {
        return withRetry(() -> given(spec)
                .formParams(account)
                .post("/createAccount"));
    }

    public Response deleteAccount(String email, String password) {
        return withRetry(() -> given(spec)
                .formParam("email", email)
                .formParam("password", password)
                .delete("/deleteAccount"));
    }

    private Response withRetry(Supplier<Response> call) {
        Response response = call.get();

        for (int retry = 1; retry <= MAX_RETRIES && RETRY_STATUSES.contains(response.statusCode()); retry++) {
            System.out.println("Retry " + retry + " after status " + response.statusCode());
            sleep(retry * 1000L);
            response = call.get();
        }
        return response;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}