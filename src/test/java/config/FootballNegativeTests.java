package config;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

// Deliberately does not extend FootballConfig: this test supplies its own (invalid) token,
// so it must not inherit a real one from the environment, and needs no FOOTBALL_DATA_API_TOKEN to run.
@Feature("Football API - Negative Tests")
public class FootballNegativeTests {

    @Before
    public void resetRestAssured() {
        // Other test classes (e.g. VideoGameConfig) set RestAssured.requestSpecification /
        // responseSpecification as JVM-static globals with an expectStatusCode(200). Surefire's
        // default class run order is filesystem-dependent and differs between OSes, so whether
        // this leftover spec leaks into this test depends on which platform runs it. Reset here
        // so the assertion below is the only thing that decides pass/fail.
        RestAssured.reset();
    }

    @Test
    @Story("Authentication")
    @Description("Returns a client error, not 200, when calling with an invalid X-Auth-Token")
    public void getTeam_InvalidToken_ReturnsClientError() {
        Response response = given()
                .baseUri("https://api.football-data.org")
                .basePath("/v4")
                .header("X-Auth-Token", "invalid-token-0000000000000000")
                .when()
                .get("/teams/57");

        assertThat(response.statusCode(), anyOf(equalTo(400), equalTo(403)));
    }
}
