package arc.assets.loaders.resolvers;

import arc.Core;
import arc.assets.loaders.FileHandleResolver;
import arc.files.Fi;

/**
 * This {@link FileHandleResolver} uses a given list of {@link Resolution}s to determine the best match based on the current
 * Screen size. An example of how this resolver works:
 *
 * <p>
 * Let's assume that we have only a single {@link Resolution} added to this resolver. This resolution has the following
 * properties:
 * </p>
 *
 * <ul>
 * <li>{@code portraitWidth = 1920}</li>
 * <li>{@code portraitHeight = 1080}</li>
 * <li>{@code folder = "1920x1080"}</li>
 * </ul>
 *
 * <p>
 * One would now supply a file to be found to the resolver. For this example, we assume it is "{@code textures/walls/brick.png}".
 * Since there is only a single {@link Resolution}, this will be the best match for any screen size. The resolver will now try to
 * find the file in the following ways:
 * </p>
 *
 * <ul>
 * <li>{@code "textures/walls/1920x1080/brick.png"}</li>
 * <li>{@code "textures/walls/brick.png"}</li>
 * </ul>
 *
 * <p>
 * The files are ultimately resolved via the given {{@link #baseResolver}. In case the first version cannot be resolved, the
 * fallback will try to search for the file without the resolution folder.
 * </p>
 */
public class ResolutionFileResolver implements FileHandleResolver{

    protected final FileHandleResolver baseResolver;
    protected final Resolution[] descriptors;
    /**
     * Creates a {@code ResolutionFileResolver} based on a given {@link FileHandleResolver} and a list of {@link Resolution}s.
     * @param baseResolver The {@link FileHandleResolver} that will ultimately used to resolve the file.
     * @param descriptors A list of {@link Resolution}s. At least one has to be supplied.
     */
    public ResolutionFileResolver(FileHandleResolver baseResolver, Resolution... descriptors){
        if(descriptors.length == 0) throw new IllegalArgumentException("At least one Resolution needs to be supplied.");
        this.baseResolver = baseResolver;
        this.descriptors = descriptors;
    }

    public static Resolution choose(Resolution... descriptors){
        int w = Core.graphics.getWidth(), h = Core.graphics.getHeight();

        // Prefer the shortest side.
        Resolution best = descriptors[0];
        if(w < h){
            for(int i = 0, n = descriptors.length; i < n; i++){
                Resolution other = descriptors[i];
                if(w >= other.portraitWidth && other.portraitWidth >= best.portraitWidth && h >= other.portraitHeight
                && other.portraitHeight >= best.portraitHeight) best = descriptors[i];
            }
        }else{
            for(int i = 0, n = descriptors.length; i < n; i++){
                Resolution other = descriptors[i];
                if(w >= other.portraitHeight && other.portraitHeight >= best.portraitHeight && h >= other.portraitWidth
                && other.portraitWidth >= best.portraitWidth) best = descriptors[i];
            }
        }
        return best;
    }

    @Override
    public Fi resolve(String fileName){
        Resolution bestResolution = choose(descriptors);
        Fi originalHandle = new Fi(fileName);
        Fi handle = baseResolver.resolve(resolve(originalHandle, bestResolution.folder));
        if(!handle.exists()) handle = baseResolver.resolve(fileName);
        return handle;
    }

    protected String resolve(Fi originalHandle, String suffix){
        String parentString = "";
        Fi parent = originalHandle.parent();
        if(parent != null && !parent.name().equals("")){
            parentString = parent + "/";
        }
        return parentString + suffix + "/" + originalHandle.name();
    }

    public static class Resolution{
        public final int portraitWidth;
        public final int portraitHeight;

        /** The name of the folder, where the assets which fit this resolution, are located. */
        public final String folder;

        /**
         * Constructs a {@code Resolution}.
         * @param portraitWidth This resolution's width.
         * @param portraitHeight This resolution's height.
         * @param folder The name of the folder, where the assets which fit this resolution, are located.
         */
        public Resolution(int portraitWidth, int portraitHeight, String folder){
            this.portraitWidth = portraitWidth;
            this.portraitHeight = portraitHeight;
            this.folder = folder;
        }
    }
}
