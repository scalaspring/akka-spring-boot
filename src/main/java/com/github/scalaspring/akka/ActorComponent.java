package com.github.scalaspring.akka;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate actor-specific beans classes.
 *
 * This is a Spring meta-annotation, the purpose of which is to make code more readable and to avoid
 * the common mistake of instantiating actors as singletons (the default for Spring beans).
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActorComponent {
}
