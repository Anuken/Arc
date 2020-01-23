package arc.ecs.fluid.gradle;

import arc.ecs.fluid.*;
import org.gradle.api.*;
import org.gradle.api.file.*;
import org.gradle.api.tasks.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Weaving wrapper for gradle.
 * @author Adrian Papari
 * @author Daan van Yperen
 */
public class FluidApiGenerationTask extends DefaultTask{

    @Input
    private File generatedSourcesDirectory;

    @Input
    private FileCollection classpath;

    @Input
    public FluidGeneratorPreferences preferences = new FluidGeneratorPreferences();

    @TaskAction
    public void fluid(){
        getLogger().info("Artemis Fluid api plugin started.");

        prepareGeneratedSourcesFolder();
        includeGeneratedSourcesInCompilation();

        new FluidGenerator().generate(
        classpathAsUrls(preferences),
        generatedSourcesDirectory, createLogAdapter(), preferences);
    }

    /**
     * bridge maven/internal logging.
     */
    private arc.ecs.fluid.generator.util.Log createLogAdapter(){
        return new arc.ecs.fluid.generator.util.Log(){
            @Override
            public void info(String msg){
                getLogger().info(msg);
            }

            @Override
            public void error(String msg){
                getLogger().error(msg);
            }
        };
    }

    /**
     * Setup generated sources folder if missing.
     */
    private void prepareGeneratedSourcesFolder(){
        if(!generatedSourcesDirectory.exists() && !generatedSourcesDirectory.mkdirs()){
            getLogger().error("Could not create " + generatedSourcesDirectory);
        }
    }

    /**
     * Must include manually, or maven buids will fail.
     */
    private void includeGeneratedSourcesInCompilation(){
//		getProject().addCompileSourceRoot(generatedSourcesDirectory().getPath());
    }

    private Set<URL> classpathAsUrls(FluidGeneratorPreferences preferences){
        try{
            Set<URL> urls = new HashSet<>();
            for(File element : classpath){
                URL url = element.toURI().toURL();
                System.out.println(url);
                if(!preferences.matchesIgnoredClasspath(url.toString())){
                    urls.add(url);
                    getLogger().info("Including: " + url);
                }
            }
            return urls;
        }catch(MalformedURLException e){
            throw new RuntimeException("failed to complete classpathAsUrls.", e);
        }
    }

    public File getGeneratedSourcesDirectory(){
        return generatedSourcesDirectory;
    }

    public void setGeneratedSourcesDirectory(File generatedSourcesDirectory){
        this.generatedSourcesDirectory = generatedSourcesDirectory;
    }

    public FileCollection getClasspath(){
        return classpath;
    }

    public void setClasspath(FileCollection classpath){
        this.classpath = classpath;
    }
}