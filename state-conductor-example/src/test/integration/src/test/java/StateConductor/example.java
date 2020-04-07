import static com.jayway.restassured.RestAssured.given;

import org.junit.Test;

public class example {

 @Test
 public void makeSureThatGoogleIsUp() {
     given().when().get("http://www.google.com").then().statusCode(200);
 }

}