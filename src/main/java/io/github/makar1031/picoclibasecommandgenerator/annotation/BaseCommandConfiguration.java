package io.github.makar1031.picoclibasecommandgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface BaseCommandConfiguration {
    String name();
    String description();
    boolean mixinStandardHelpOptions() default true;
    PackageDetectionStrategy packageDetectionStrategy() default PackageDetectionStrategy.SAME_FIRST_COMMAND;
    String packageName() default "";
    String className() default "BaseCommand";
}
