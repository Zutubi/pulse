package com.cinnamonbob.core;

import com.cinnamonbob.core.model.Property;

import java.io.InputStream;
import java.util.List;

/**
 * Utility code to aid in the loading of bob files.
 */
public class BobFileLoader
{

    public static BobFile load(InputStream input) throws BobException
    {
        return load(input, null);
    }

    public static BobFile load(InputStream input, FileResourceRepository fileResourceRepository) throws BobException
    {
        return load(input, fileResourceRepository, null);
    }

    public static BobFile load(InputStream input, ResourceRepository resourceRepository, List<Reference> references) throws BobException
    {
        FileLoader loader = createLoader(resourceRepository);
        BobFile result = new BobFile();
        loader.load(input, result, references);
        return result;
    }

    private static FileLoader createLoader(ResourceRepository resourceRepository)
    {
        FileLoader fileLoader = new FileLoader();
        fileLoader.register("property", Property.class);
        fileLoader.register("recipe", Recipe.class);
        fileLoader.register("def", ComponentDefinition.class);
        fileLoader.register("post-processor", PostProcessorGroup.class);
        fileLoader.register("command", CommandGroup.class);
        fileLoader.register("regex", RegexPostProcessor.class);
        fileLoader.register("executable", ExecutableCommand.class);
        fileLoader.register("resource", ResourceReference.class);

        fileLoader.setObjectFactory(new ObjectFactory());
        fileLoader.setResourceRepository(resourceRepository);

        return fileLoader;
    }

}
