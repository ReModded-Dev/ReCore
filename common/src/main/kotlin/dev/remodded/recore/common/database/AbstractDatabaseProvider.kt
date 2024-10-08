package dev.remodded.recore.common.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.remodded.recore.api.database.DatabaseProvider
import dev.remodded.recore.api.database.DatabaseType
import dev.remodded.recore.common.Constants
import org.slf4j.LoggerFactory


/**
 * Abstract class representing a database provider.
 *
 * @param databaseConnectionConfig The configuration for the database connection.
 * @property databaseType The type of the database being used.
 * @param dataSourceClassName The fully qualified name of the JDBC data source class.
 */
abstract class AbstractDatabaseProvider(
    databaseConnectionConfig: DatabaseConnectionConfig,
    private val databaseType: DatabaseType,
    dataSourceClassName: String
) : DatabaseProvider {

    private val logger = LoggerFactory.getLogger(Constants.NAME)

    // Hikari JDBC data source
    private val dataSource: HikariDataSource


    // Initialize the database config and connection pool
    init {
        logger.info("Initializing $databaseType database connection...")

        val config = HikariConfig().apply {
            this.dataSourceClassName = dataSourceClassName
            username = databaseConnectionConfig.username
            password = databaseConnectionConfig.password

            addDataSourceProperty("serverName", databaseConnectionConfig.hostname)
            addDataSourceProperty("portNumber", databaseConnectionConfig.port)
            addDataSourceProperty("databaseName", databaseConnectionConfig.database)
            poolName = "ReCore"

            maximumPoolSize = databaseConnectionConfig.maxConnectionPoolSize
        }

        dataSource = HikariDataSource(config)
    }

    /**
     * Method to retrieve the client connection. If the data source is not initialized,
     * it throws a DataSourceNotInitializedException.
     *
     * @return HikariDataSource client
     */
    override fun getDataSource(): HikariDataSource = dataSource

    /**
     * Method to get the database type.
     *
     * @return Returns the type of the database being used.
     */
    override fun getType(): DatabaseType = databaseType
}
