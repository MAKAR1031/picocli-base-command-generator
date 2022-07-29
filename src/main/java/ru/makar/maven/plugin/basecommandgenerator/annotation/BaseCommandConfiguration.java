package ru.makar.maven.plugin.basecommandgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static ru.makar.maven.plugin.basecommandgenerator.annotation.PackageDetectionStrategy.SAME_FIRST_COMMAND;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface BaseCommandConfiguration {
    String name();
    String description();
    boolean mixinStandardHelpOptions() default true;
    PackageDetectionStrategy packageDetectionStrategy() default SAME_FIRST_COMMAND;
    String packageName() default "";
    String className() default "BaseCommand";
}
