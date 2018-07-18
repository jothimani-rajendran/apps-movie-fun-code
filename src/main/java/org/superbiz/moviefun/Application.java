package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials() {
        return new DatabaseServiceCredentials(System.getenv("VCAP_SERVICES"));
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig hikariConfig  = new HikariConfig();
        hikariConfig.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        return hikariDataSource;
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig hikariConfig  = new HikariConfig();
        hikariConfig.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql", "p-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        return hikariDataSource;
    }
    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter(){
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setDatabase(Database.MYSQL);
        return adapter;
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean moviesContainerEntityManagerFactoryBean(HibernateJpaVendorAdapter hibernateJpaVendorAdapter,DataSource moviesDataSource ){
        LocalContainerEntityManagerFactoryBean eManager = new LocalContainerEntityManagerFactoryBean();
        eManager.setDataSource(moviesDataSource);
        eManager.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        eManager.setPackagesToScan("org.superbiz.moviefun.movies");
        eManager.setPersistenceUnitName("movies-persistence");
        return eManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsContainerEntityManagerFactoryBean(HibernateJpaVendorAdapter hibernateJpaVendorAdapter,DataSource albumsDataSource ){
        LocalContainerEntityManagerFactoryBean eManager = new LocalContainerEntityManagerFactoryBean();
        eManager.setDataSource(albumsDataSource);
        eManager.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        eManager.setPackagesToScan("org.superbiz.moviefun.albums");
        eManager.setPersistenceUnitName("albums-persistence");
        return eManager;
    }

    @Bean
    public PlatformTransactionManager moviesTransactionManager(EntityManagerFactory moviesContainerEntityManagerFactoryBean){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(moviesContainerEntityManagerFactoryBean);
        return jpaTransactionManager;
    }

    @Bean
    public PlatformTransactionManager albumsTransactionManager(EntityManagerFactory albumsContainerEntityManagerFactoryBean){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(albumsContainerEntityManagerFactoryBean);
        return jpaTransactionManager;
    }
    @Bean
    public TransactionOperations albumsTransactionOperations(PlatformTransactionManager albumsTransactionManager){
        TransactionTemplate transactionTemplate = new TransactionTemplate(albumsTransactionManager);
        return transactionTemplate;
    }

    @Bean
    public TransactionOperations moviesTransactionOperations(PlatformTransactionManager moviesTransactionManager){
        TransactionTemplate transactionTemplate = new TransactionTemplate(moviesTransactionManager);
        return transactionTemplate;
    }
}
