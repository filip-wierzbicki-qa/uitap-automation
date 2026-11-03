package client;

import config.Config;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class AutomationExerciseClient {

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
        return given(spec).get("/productsList");
    }

    public Response postProducts() {
        return given(spec).post("/productsList");
    }

    public Response searchProducts(String term){
        RequestSpecification request = given(spec);
        if (term != null) {
            request.formParam("search_product", term);
        }
        return request.post("/searchProduct");
    }

    public Response verifyLogin(String email, String password){
        RequestSpecification request = given(spec);
        if (email != null){
            request.formParam("email", email);
        }
        if (password != null){
            request.formParam("password", password);
        }
        return request.post("/verifyLogin");
    }

    public Response createAccount(Map<String, String> account) {
        return given(spec)
                .formParams(account)
                .post("/createAccount");
    }

    public Response deleteAccount(String email, String password) {
        return given(spec)
                .formParam("email", email)
                .formParam("password", password)
                .delete("/deleteAccount");
    }


}