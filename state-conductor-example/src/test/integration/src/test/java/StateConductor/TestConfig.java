package dcgssof;

import com.marklogic.client.helper.DatabaseClientConfig;
import com.marklogic.client.helper.DatabaseClientProvider;
import com.marklogic.client.helper.LoggingObject;
import com.marklogic.client.modulesloader.impl.DefaultModulesLoader;
import com.marklogic.client.modulesloader.impl.XccAssetLoader;
import com.marklogic.client.modulesloader.ModulesLoader;
import com.marklogic.client.modulesloader.tokenreplacer.RoxyModuleTokenReplacer;
import com.marklogic.client.spring.SimpleDatabaseClientProvider;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static io.restassured.RestAssured.digest;

/**
 * Spring test configuration class that does the following:
 * <ol>
 * <li>Reads Roxy property files to get connection information</li>
 * <li>Sets up a Java Client DatabaseClient against port 8000 using admin so you can fiddle with the database</li>
 * <li>Initializes RestAssured against the application port</li>
 * </ol>
 */
@Configuration
@PropertySource(value = {"file:src/test/integration/src/test/java/StateConductor/test.properties", "file:gradle-local.properties" }, ignoreResourceNotFound = true)
public class TestConfig extends LoggingObject implements InitializingBean {

	@Value("${mlUsername:admin}")
	public String username;

	@Value("${mlPassword:admin}")
	public String password;

	@Value("${mlHost:localhost}")
	public String host;

	public Integer restPort = 8000;

	@Value("${mlTestRestPort:8011}")
	public Integer testPort;

	/*
	@Value("${testDatabase:state-conductor-example-test-content}")
	public String testDatabase;
	*/
	private String test_user = username;

	private String test_password = password;

	/**
	 * Has to be static so that Spring instantiates it first. This allows it to read from the Roxy property files and
	 * bind values to the Value annotations.
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
		PropertySourcesPlaceholderConfigurer c = new PropertySourcesPlaceholderConfigurer();
		c.setIgnoreResourceNotFound(true);
		return c;
	}

	/**
	 * Called by Spring when the test suite starts up.
	 */
	@Override
	public void afterPropertiesSet() {
		RestAssured.baseURI = "http://" + host;
		RestAssured.port = testPort;
		RestAssured.authentication = RestAssured.preemptive().basic(test_user, test_password);
		RestAssured.filters(new PerformanceFilter());
		logger.info("Initialized RestAssured at: " + RestAssured.baseURI + ":" + RestAssured.port);
	}

	/**
	 * DatabaseClientConfig is from ml-javaclient-util, just a handy container for connection properties.
	 *
	 * @return
	 */
	@Bean
	public DatabaseClientConfig databaseClientConfig() {
		DatabaseClientConfig config = new DatabaseClientConfig(host, restPort, username, password);
		//config.setDatabase(testDatabase);
		return config;
	}

	/**
	 * Needed by ml-junit for setting up a DatabaseClient instance.
	 *
	 * @return
	 */
	@Bean
	public DatabaseClientProvider databaseClientProvider() {
		return new SimpleDatabaseClientProvider(databaseClientConfig());
	}

	/**
	 * This is included by default so that ModulesLoaderTestExecutionListener can be used.
	 *
	 * @return
	 */
	@Bean
	public ModulesLoader modulesLoader() {
		XccAssetLoader assetLoader = new XccAssetLoader();
		assetLoader.setUsername(username);
		assetLoader.setPassword(password);
		assetLoader.setHost(host);
		assetLoader.setPort(restPort);
		assetLoader.setDatabaseName("state-conductor-example-modules");

		RoxyModuleTokenReplacer replacer = new RoxyModuleTokenReplacer();
		assetLoader.setModuleTokenReplacer(replacer);

		return new DefaultModulesLoader(assetLoader);
	}
}
