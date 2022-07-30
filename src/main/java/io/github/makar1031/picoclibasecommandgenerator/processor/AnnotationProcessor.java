package io.github.makar1031.picoclibasecommandgenerator.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import io.github.makar1031.picoclibasecommandgenerator.annotation.BaseCommandConfiguration;
import io.github.makar1031.picoclibasecommandgenerator.annotation.VersionProvider;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("picocli.CommandLine.Command")
public class AnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        if (annotations.isEmpty()) {
            return false;
        }
        final BaseCommandConfiguration configuration = getConfiguration(environment);
        if (configuration == null) {
            return false;
        }

        final TypeElement annotation = annotations.iterator().next();
        final Set<? extends Element> commands = environment.getElementsAnnotatedWith(annotation);
        final Element provider = getVersionProvider(environment);
        final String packageName = switch (configuration.packageDetectionStrategy()) {
            case SPECIFIED -> configuration.packageName();
            case SAME_FIRST_COMMAND -> processingEnv.getElementUtils()
                .getPackageOf(commands.iterator().next())
                .getQualifiedName()
                .toString();
        };
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalStateException("Package for base command not resolved");
        }

        final AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(ClassName.get(annotation))
            .addMember("name", "$S", configuration.name())
            .addMember("description", "$S", configuration.description())
            .addMember("mixinStandardHelpOptions", "$L", configuration.mixinStandardHelpOptions())
            .addMember("subcommands", commands.stream()
                .map(c -> CodeBlock.of("$T.class", c))
                .collect(CodeBlock.joining(",", "{", "}"))
            );
        if (provider != null) {
            annotationBuilder.addMember("versionProvider", "$T.class", provider);
        }

        final JavaFile javaFile = JavaFile.builder(
            packageName,
            TypeSpec.classBuilder(configuration.className())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationBuilder.build())
                .build()
        ).build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private BaseCommandConfiguration getConfiguration(RoundEnvironment environment) {
        final Set<? extends Element> elements = environment.getElementsAnnotatedWith(BaseCommandConfiguration.class);
        if (elements.isEmpty()) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "Base command configuration not found, generation will be skipped"
            );
            return null;
        }
        return elements.iterator()
            .next()
            .getAnnotation(BaseCommandConfiguration.class);
    }

    private Element getVersionProvider(RoundEnvironment environment) {
        final Set<? extends Element> providers = environment.getElementsAnnotatedWith(VersionProvider.class);
        if (providers.isEmpty()) {
            return null;
        }
        if (providers.size() > 1) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "Found multiple version providers, the first one will be used"
            );
        }
        return providers.iterator().next();
    }
}
