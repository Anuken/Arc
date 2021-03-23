package arc.maps;

/** Layer for a TiledMap */
public class TileLayer extends MapLayer{
    public final int width;
    public final int height;
    public final float tileWidth;
    public final float tileHeight;

    private Cell[][] cells;

    /**
     * Creates TiledMap layer
     * @param width layer width in tiles
     * @param height layer height in tiles
     * @param tileWidth tile width in pixels
     * @param tileHeight tile height in pixels
     */
    public TileLayer(int width, int height, int tileWidth, int tileHeight){
        super();
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.cells = new Cell[width][height];
    }

    /**
     * @param x X coordinate
     * @param y Y coordinate
     * @return {@link Cell} at (x, y)
     */
    public Cell getCell(int x, int y){
        if(x < 0 || x >= width || y < 0 || y >= height) return null;
        return cells[x][y];
    }

    /**
     * Sets the {@link Cell} at the given coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param cell the {@link Cell} to set at the given coordinates.
     */
    public void setCell(int x, int y, Cell cell){
        if(x < 0 || x >= width || y < 0 || y >= height) return;
        cells[x][y] = cell;
    }

    public MapTile getTile(int x, int y){
        return getCell(x, y) == null ? null : getCell(x, y).tile;
    }

    /** represents a cell in a TiledLayer: TiledMapTile, flip and rotation properties. */
    public static class Cell{
        public static final int ROTATE_0 = 0;
        public static final int ROTATE_90 = 1;
        public static final int ROTATE_180 = 2;
        public static final int ROTATE_270 = 3;

        public MapTile tile;
        public boolean flipHorizontally;
        public boolean flipVertically;
        public int rotation;
    }
}
