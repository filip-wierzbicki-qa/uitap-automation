package client;

import config.Config;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

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
}