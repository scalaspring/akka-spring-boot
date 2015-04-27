package com.github.scalaspring.akka;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate actor beans (not actor references!) within a Spring configuration.
 *
 * This annotation SHOULD only be required for Actor-based classes used from third-party
 * libraries, etc. Please use the {@code ActorComponent} annotation to annotate your
 * classes instead if you're creating your own actors for use with Spring.
 *
 * @see ActorComponent
 */
@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActorBean {
    /**
     * The name of this bean, or if plural, aliases for this bean. If left unspecified
     * the name of the bean is the name of the annotated method. If specified, the method
     * name is ignored.
     */
    String[] name() default {};

    /**
     * Are dependencies to be injected via convention-based autowiring by name or type?
     */
    Autowire autowire() default Autowire.NO;

    /**
     * The optional name of a method to call on the bean instance during initialization.
     * Not commonly used, given that the method may be called programmatically directly
     * within the body of a Bean-annotated method.
     * <p>The default value is {@code ""}, indicating no init method to be called.
     */
    String initMethod() default "";

    /**
     * The optional name of a method to call on the bean instance upon closing the
     * application context, for example a {@code close()} method on a JDBC
     * {@code DataSource} implementation, or a Hibernate {@code SessionFactory} object.
     * The method must have no arguments but may throw any exception.
     * <p>As a convenience to the user, the container will attempt to infer a destroy
     * method against an object returned from the {@code @Bean} method. For example, given
     * an {@code @Bean} method returning an Apache Commons DBCP {@code BasicDataSource},
     * the container will notice the {@code close()} method available on that object and
     * automatically register it as the {@code destroyMethod}. This 'destroy method
     * inference' is currently limited to detecting only public, no-arg methods named
     * 'close' or 'shutdown'. The method may be declared at any level of the inheritance
     * hierarchy and will be detected regardless of the return type of the {@code @Bean}
     * method (i.e., detection occurs reflectively against the bean instance itself at
     * creation time).
     * <p>To disable destroy method inference for a particular {@code @Bean}, specify an
     * empty string as the value, e.g. {@code @Bean(destroyMethod="")}. Note that the
     * {@link org.springframework.beans.factory.DisposableBean} and the
     * {@link java.io.Closeable}/{@link java.lang.AutoCloseable} interfaces will
     * nevertheless get detected and the corresponding destroy/close method invoked.
     * <p>Note: Only invoked on beans whose lifecycle is under the full control of the
     * factory, which is always the case for singletons but not guaranteed for any
     * other scope.
     * @see org.springframework.context.ConfigurableApplicationContext#close()
     */
    String destroyMethod() default AbstractBeanDefinition.INFER_METHOD;
}
