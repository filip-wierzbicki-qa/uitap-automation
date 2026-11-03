package tests;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertTrue;

public class SearchTest extends BaseTest {

    @DataProvider(name = "searchTerms")
    public Object[][] searchTerms() {
        return new Object[][] {
                {"top"},
                {"jean"},
                {"dress"}
        };
    }

    @Test(dataProvider = "searchTerms", groups = {"smoke", "regression"})
    public void searchProductReturnsResults(String term) {
        Response response = api.searchProducts(term);

        response.then()
                .statusCode(200)
                .body("responseCode", equalTo(200))
                .body("products", not(empty()));

        JsonPath json = response.jsonPath();
        List<String> names = json.getList("products.name", String.class);
        List<String> categories = json.getList("products.category.category", String.class);

        for (int i = 0; i < names.size(); i++) {
            String searchable = (names.get(i) + " " + categories.get(i)).toLowerCase();
            assertTrue(searchable.contains(term), "'" + term + "' not found in: " + searchable);
        }
    }
}