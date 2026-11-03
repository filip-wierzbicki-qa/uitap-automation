package tests;

import client.AutomationExerciseClient;
import org.testng.annotations.BeforeClass;

public class BaseTest {

    protected AutomationExerciseClient api;

    @BeforeClass(alwaysRun = true)
    public void createClient() {
        api = new AutomationExerciseClient();
    }
}