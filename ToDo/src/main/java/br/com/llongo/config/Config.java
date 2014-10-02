package br.com.llongo.config;

import java.util.Properties;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
// Marks this class as configuration
// Specifies which package to scan
@ComponentScan("br.com.llongo")
// Enables Spring's annotations
@EnableWebMvc
@EnableJpaRepositories("br.com.llongo.persistence.repository")
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class Config extends WebMvcConfigurerAdapter {
	private static final String PROPERTY_NAME_DATABASE_DRIVER = "db.driver";
	private static final String PROPERTY_NAME_DATABASE_PASSWORD = "db.password";
	private static final String PROPERTY_NAME_DATABASE_URL = "db.url";
	private static final String PROPERTY_NAME_DATABASE_USERNAME = "db.username";

	private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "hibernate.dialect";
	private static final String PROPERTY_NAME_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
	@Resource
	private Environment env;

	@Bean
	public InternalResourceViewResolver configureInternalResourceViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/libs/**").addResourceLocations("/libs/");
		registry.addResourceHandler("/app/**").addResourceLocations("/app/");
		registry.addResourceHandler("/styles/**").addResourceLocations("/app/styles/");
		registry.addResourceHandler("/scripts/**").addResourceLocations("/app/scripts/");
		registry.addResourceHandler("/views/**").addResourceLocations("/app/views/");
		registry.addResourceHandler("/assets/**").addResourceLocations(
				"/assets/");
	}

	Properties additionalProperties() {
		Properties properties = new Properties();
		properties.setProperty(PROPERTY_NAME_HIBERNATE_DIALECT,
				env.getProperty(PROPERTY_NAME_HIBERNATE_DIALECT));
		properties.setProperty(PROPERTY_NAME_HIBERNATE_SHOW_SQL,
				env.getProperty(PROPERTY_NAME_HIBERNATE_SHOW_SQL));
		return properties;
	}

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env
				.getRequiredProperty(PROPERTY_NAME_DATABASE_DRIVER));
		dataSource.setUrl(env.getRequiredProperty(PROPERTY_NAME_DATABASE_URL));
		dataSource.setUsername(env
				.getRequiredProperty(PROPERTY_NAME_DATABASE_USERNAME));
		dataSource.setPassword(env
				.getRequiredProperty(PROPERTY_NAME_DATABASE_PASSWORD));

		return dataSource;
	}

	@Bean
	public Flyway initDbMigrate() {
		Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource());
		flyway.setInitOnMigrate(true);
		flyway.migrate();
		return flyway;
	}

	@Bean
	public EntityManagerFactory entityManagerFactory() {

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabase(Database.MYSQL);
		vendorAdapter.setDatabasePlatform(env
				.getProperty(PROPERTY_NAME_HIBERNATE_DIALECT));
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("br.com.llongo.persistence");
		factory.setDataSource(dataSource());
		factory.setJpaProperties(additionalProperties());
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory());
		return txManager;
	}
}