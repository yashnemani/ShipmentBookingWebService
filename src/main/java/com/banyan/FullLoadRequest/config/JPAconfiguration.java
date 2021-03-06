package com.banyan.FullLoadRequest.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaDialect;
/*import org.springframework.orm.jpa.JpaDialect;*/
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.banyan.FullLoadRequest.models.enums.Databases;

@Configuration
@EnableTransactionManagement
@EnableScheduling
public class JPAconfiguration {

	@Autowired
	public DataSource dataSource;
	@Autowired
	JpaDialect jpaDialect;

	@Bean
	public DataSource dataSource() {

		// DEV DB
		  final DriverManagerDataSource devDataSource = new DriverManagerDataSource();
		  devDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
		  devDataSource.setUrl("jdbc:oracle:thin:@nfr-win-orat01:1521:xdev");
		  devDataSource.setUsername("tbb"); devDataSource.setPassword("test02"); 
		// Production DB
		final DriverManagerDataSource prodDataSource = new DriverManagerDataSource();
		prodDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
		prodDataSource.setUrl("jdbc:oracle:thin:@nfr-win-orap02:1521:titan");
		prodDataSource.setUsername("tbb");
		prodDataSource.setPassword("2lgbbt2");
		
		Map<Object, Object> targetDataSources = new HashMap<>();
		targetDataSources.put(Databases.Dev, devDataSource);
		targetDataSources.put(Databases.Prod, prodDataSource);
		RoutingDataSourceExtractor routingDataSource = new RoutingDataSourceExtractor();
		routingDataSource.setTargetDataSources(targetDataSources);
		routingDataSource.setDefaultTargetDataSource(prodDataSource);
		return routingDataSource;
	}

	@Bean
	LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Environment env) {

		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(dataSource);
		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		entityManagerFactoryBean.setPackagesToScan("com.banyan.FullLoadRequest");
		Properties jpaProperties = new Properties();

		// Configures the used database dialect. This allows Hibernate to create SQL
		// that is optimized for the used database.
		System.out.println(env.getRequiredProperty("hibernate.dialect"));
		jpaProperties.put("hibernate.dialect", env.getRequiredProperty("hibernate.dialect"));

		// Specifies the action that is invoked to the database when the Hibernate
		// SessionFactory is created or closed.
		jpaProperties.put("hibernate.hbm2ddl.auto", env.getRequiredProperty("hibernate.hbm2ddl.auto"));

		// Configures the naming strategy that is used when Hibernate creates
		// new database objects and schema elements
		jpaProperties.put("hibernate.ejb.naming_strategy", env.getRequiredProperty("hibernate.ejb.naming_strategy"));

		// If the value of this property is true, Hibernate writes all SQL
		// statements to the console.
		jpaProperties.put("hibernate.show_sql", env.getRequiredProperty("hibernate.show_sql"));

		// If the value of this property is true, Hibernate will format the SQL
		// that is written to the console.
		jpaProperties.put("hibernate.format_sql", env.getRequiredProperty("hibernate.format_sql"));

		entityManagerFactoryBean.setJpaProperties(jpaProperties);

		return entityManagerFactoryBean;
	}

	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	JpaDialect jpaDialect() {
		return new HibernateJpaDialect();
	}

	@Bean
	JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {

		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);
		transactionManager.setJpaDialect(jpaDialect);
		return transactionManager;
	}
}
