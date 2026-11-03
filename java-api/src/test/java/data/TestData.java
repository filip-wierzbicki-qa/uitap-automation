package data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestData {

    public static Map<String, String> newAccount() {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        Map<String, String> account = new HashMap<>();
        account.put("name", "Test User " + unique);
        account.put("email", "uitap.test." + unique + "@example.com");
        account.put("password", "Pw-" + unique);
        account.put("title", "Mr");
        account.put("birth_date", "15");
        account.put("birth_month", "6");
        account.put("birth_year", "1990");
        account.put("firstname", "Test");
        account.put("lastname", "User");
        account.put("company", "Test Company");
        account.put("address1", "Teststrasse 1");
        account.put("address2", "");
        account.put("country", "India");
        account.put("zipcode", "8000");
        account.put("state", "Zurich");
        account.put("city", "Zurich");
        account.put("mobile_number", "0790000000");
        return account;
    }

}
