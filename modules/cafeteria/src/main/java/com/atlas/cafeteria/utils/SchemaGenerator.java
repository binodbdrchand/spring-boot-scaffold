package com.atlas.cafeteria.utils;

import jakarta.persistence.Entity;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.SourceType;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.internal.ExceptionHandlerHaltImpl;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.ExceptionHandler;
import org.hibernate.tool.schema.spi.SchemaCreator;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.ScriptSourceInput;
import org.hibernate.tool.schema.spi.SourceDescriptor;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

public final class SchemaGenerator {

    private static final String BASE_PACKAGE = "com.atlas.cafeteria";

    private SchemaGenerator() {
    }

    static void main(String[] args) {
        String outputPath = args.length > 0
                ? args[0]
                : "build/generated/schema.sql";

        File outputFile = new File(outputPath);

        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException(
                    "Unable to create directory: " + parent
            );
        }

        StandardServiceRegistry registry =
                new StandardServiceRegistryBuilder()
                        .applySetting(
                                "hibernate.dialect",
                                "org.hibernate.dialect.PostgreSQLDialect"
                        )
                        .build();

        try {
            MetadataSources metadataSources =
                    new MetadataSources(registry);

            ClassPathScanningCandidateComponentProvider scanner =
                    new ClassPathScanningCandidateComponentProvider(false);

            scanner.addIncludeFilter(
                    new AnnotationTypeFilter(Entity.class)
            );

            scanner.findCandidateComponents(BASE_PACKAGE)
                    .forEach(candidate -> {
                        try {
                            Class<?> entityClass =
                                    Class.forName(candidate.getBeanClassName());

                            metadataSources.addAnnotatedClass(entityClass);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException(
                                    "Unable to load entity: "
                                            + candidate.getBeanClassName(),
                                    e
                            );
                        }
                    });

            Metadata metadata = metadataSources.buildMetadata();

            SchemaManagementTool schemaManagementTool =
                    Objects.requireNonNull(
                            registry.getService(SchemaManagementTool.class),
                            "Hibernate SchemaManagementTool is not available"
                    );

            SchemaCreator schemaCreator =
                    schemaManagementTool.getSchemaCreator(
                            Collections.emptyMap()
                    );

            ScriptTargetOutputToFile scriptOutput =
                    new ScriptTargetOutputToFile(
                            outputFile,
                            "UTF-8",
                            false
                    );

            TargetDescriptor targetDescriptor =
                    new TargetDescriptor() {

                        @Override
                        public EnumSet<TargetType> getTargetTypes() {
                            return EnumSet.of(TargetType.SCRIPT);
                        }

                        @Override
                        public org.hibernate.tool.schema.spi.ScriptTargetOutput
                        getScriptTargetOutput() {
                            return scriptOutput;
                        }
                    };

            SourceDescriptor sourceDescriptor =
                    new SourceDescriptor() {

                        @Override
                        public SourceType getSourceType() {
                            return SourceType.METADATA;
                        }

                        @Override
                        public ScriptSourceInput getScriptSourceInput() {
                            return null;
                        }
                    };

            ExecutionOptions executionOptions =
                    new ExecutionOptions() {

                        @Override
                        public boolean shouldManageNamespaces() {
                            return true;
                        }

                        @Override
                        public Map<String, Object> getConfigurationValues() {
                            return Collections.emptyMap();
                        }

                        @Override
                        public ExceptionHandler getExceptionHandler() {
                            return ExceptionHandlerHaltImpl.INSTANCE;
                        }
                    };

            schemaCreator.doCreation(
                    metadata,
                    executionOptions,
                    ContributableMatcher.ALL,
                    sourceDescriptor,
                    targetDescriptor
            );

            System.out.println(
                    "Schema generated: " + outputFile.getAbsolutePath()
            );

        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}