package com.halversondm;

import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

@Component
public class FileTemplateMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTemplateMain.class);

    public void processTemplates() throws Exception {
        Configuration cfg = getConfiguration();
        File[] generatorFiles = getGeneratorFiles();
        for (File file : generatorFiles) {
            processGenerator(cfg, file);
            // error handling logic delete all files if one is unsuccessful
        }

    }

    private void processGenerator(Configuration cfg, File generator) throws IOException, TemplateException {
        FileUrlResource generatorFile = new FileUrlResource(generator.getAbsolutePath());
        Properties generatorProps = PropertiesLoaderUtils.loadProperties(generatorFile);

        // error logic on the generatorFile, validation on required properties
        // stop and delete everything if no bueno

        String inputPropertiesFile = (String) generatorProps.get("inputPropertiesFile");
        String inputTemplateFile = (String) generatorProps.get("inputTemplateFile");
        String outputFile = (String) generatorProps.get("outputFile");
        if (inputPropertiesFile == null || inputTemplateFile == null || outputFile == null) {
            throw new RuntimeException(String.format("For file %s, all properties are required.", generator.getAbsolutePath()));
        }

        ClassPathResource resource = new ClassPathResource(inputPropertiesFile);
        Properties props = PropertiesLoaderUtils.loadProperties(resource);

        LOGGER.info("Creating file '{}' using properties '{}' and template '{}'", outputFile, inputPropertiesFile, inputTemplateFile);
        LOGGER.debug("Properties as an object {}", props);
        Template template = cfg.getTemplate(inputTemplateFile);
        try (Writer fileWriter = new FileWriter(new File(outputFile))) {
            template.process(props, fileWriter);
        }
    }

    private Configuration getConfiguration() {
        Configuration cfg = new Configuration(new Version(2, 3, 20));
        cfg.setClassLoaderForTemplateLoading(FileTemplateMain.class.getClassLoader(), "templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return cfg;
    }

    private File[] getGeneratorFiles() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("generators");
        String path = url != null ? url.getPath() : "";
        return new File(path).listFiles();
    }

}
