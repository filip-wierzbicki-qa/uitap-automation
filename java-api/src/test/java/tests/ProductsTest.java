package tests;

import org.testng.annotations.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ProductsTest extends BaseTest {

    @Test(groups = {"smoke", "regression"})
    public void getAllProductsReturnsList(){
        api.getProducts()
                .then()
                .statusCode(200)
                .body("responseCode", equalTo(200))
                .body("products", not(empty()));
    }

    @Test(groups = {"regression"})
    public void postToProductsListIsNotSupported(){
        // This API reports errors in the JSON body, not in the HTTP status
        api.postProducts()
                .then()
                .statusCode(200)
                .body("responseCode", equalTo(405))
                .body("message", equalTo("This request method is not supported."));
    }
}