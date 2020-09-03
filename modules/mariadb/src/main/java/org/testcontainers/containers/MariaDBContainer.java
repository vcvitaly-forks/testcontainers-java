package org.testcontainers.containers;

import org.testcontainers.utility.DockerImageName;

/**
 * Container implementation for the MariaDB project.
 *
 * @author Miguel Gonzalez Sanchez
 */
public class MariaDBContainer<SELF extends MariaDBContainer<SELF>> extends JdbcDatabaseContainer<SELF> {

    public static final String NAME = "mariadb";
    public static final String IMAGE = "mariadb";
    public static final String DEFAULT_TAG = "10.3.6";

    static final String DEFAULT_USER = "test";

    static final String DEFAULT_PASSWORD = "test";

    static final Integer MARIADB_PORT = 3306;
    private String databaseName = "test";
    private String username = DEFAULT_USER;
    private String password = DEFAULT_PASSWORD;
    private static final String MARIADB_ROOT_USER = "root";
    private static final String MY_CNF_CONFIG_OVERRIDE_PARAM_NAME = "TC_MY_CNF";

    /**
     * @deprecated use {@link MariaDBContainer(DockerImageName)} instead
     */
    @Deprecated
    public MariaDBContainer() {
        this(IMAGE + ":" + DEFAULT_TAG);
    }

    /**
     * @deprecated use {@link MariaDBContainer(DockerImageName)} instead
     */
    @Deprecated
    public MariaDBContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public MariaDBContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        addExposedPort(MARIADB_PORT);
    }

    @Override
    protected Integer getLivenessCheckPort() {
        return getMappedPort(MARIADB_PORT);
    }

    @Override
    protected void configure() {
        optionallyMapResourceParameterAsVolume(MY_CNF_CONFIG_OVERRIDE_PARAM_NAME, "/etc/mysql/conf.d", "mariadb-default-conf");

        addEnv("MYSQL_DATABASE", databaseName);
        addEnv("MYSQL_USER", username);
        if (password != null && !password.isEmpty()) {
            addEnv("MYSQL_PASSWORD", password);
            addEnv("MYSQL_ROOT_PASSWORD", password);
        } else if (MARIADB_ROOT_USER.equalsIgnoreCase(username)) {
            addEnv("MYSQL_ALLOW_EMPTY_PASSWORD", "yes");
        } else {
            throw new ContainerLaunchException("Empty password can be used only with the root user");
        }
        setStartupAttempts(3);
    }

    @Override
    public String getDriverClassName() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    public String getJdbcUrl() {
        String additionalUrlParams = constructUrlParameters("?", "&");
        return "jdbc:mariadb://" + getHost() + ":" + getMappedPort(MARIADB_PORT) +
            "/" + databaseName + additionalUrlParams;
    }

    @Override
    public String getDatabaseName() {
    	return databaseName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getTestQueryString() {
        return "SELECT 1";
    }

    public SELF withConfigurationOverride(String s) {
        parameters.put(MY_CNF_CONFIG_OVERRIDE_PARAM_NAME, s);
        return self();
    }

    @Override
    public SELF withDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
        return self();
    }

    @Override
    public SELF withUsername(final String username) {
        this.username = username;
        return self();
    }

    @Override
    public SELF withPassword(final String password) {
        this.password = password;
        return self();
    }
}
