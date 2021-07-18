package arc.math.geom;

import arc.struct.Seq;
import arc.func.Cons;
import arc.math.geom.QuadTree.QuadTreeObject;

import java.util.Iterator;

/**
 * A basic quad tree.
 * <p>
 * This class represents any node, but you will likely only interact with the root node.
 *
 * @param <T> The type of object this quad tree should contain. An object only requires some way of getting rough bounds.
 * @author xSke
 * @author Anuke
 */
public class QuadTree<T extends QuadTreeObject>{
    protected final Rect tmp = new Rect();
    protected static final int maxObjectsPerNode = 5;

    public Rect bounds;
    public Seq<T> objects = new Seq<>(false);
    public QuadTree<T> botLeft, botRight, topLeft, topRight;
    public boolean leaf = true;

    public QuadTree(Rect bounds){
        this.bounds = bounds;
    }

    private void split(){
        if(!leaf) return;

        float subW = bounds.width / 2;
        float subH = bounds.height / 2;

        if(botLeft == null){
            botLeft = newChild(new Rect(bounds.x, bounds.y, subW, subH));
            botRight = newChild(new Rect(bounds.x + subW, bounds.y, subW, subH));
            topLeft = newChild(new Rect(bounds.x, bounds.y + subH, subW, subH));
            topRight = newChild(new Rect(bounds.x + subW, bounds.y + subH, subW, subH));
        }
        leaf = false;

        // Transfer objects to children if they fit entirely in one
        for(Iterator<T> iterator = objects.iterator(); iterator.hasNext();){
            T obj = iterator.next();
            hitbox(obj);
            QuadTree<T> child = getFittingChild(tmp);
            if(child != null){
                child.insert(obj);
                iterator.remove();
            }
        }
    }

    private void unsplit(){
        if(leaf) return;
        objects.addAll(botLeft.objects);
        objects.addAll(botRight.objects);
        objects.addAll(topLeft.objects);
        objects.addAll(topRight.objects);
        botLeft.clear();
        botRight.clear();
        topLeft.clear();
        topRight.clear();
        leaf = true;
    }

    /**
     * Inserts an object into this node or its child nodes. This will split a leaf node if it exceeds the object limit.
     */
    public void insert(T obj){
        hitbox(obj);
        if(!bounds.overlaps(tmp)){
            // New object not in quad tree, ignoring
            // throw an exception?
            return;
        }

        if(leaf && objects.size + 1 > maxObjectsPerNode) split();

        if(leaf){
            // Leaf, so no need to add to children, just add to root
            objects.add(obj);
        }else{
            hitbox(obj);
            // Add to relevant child, or root if can't fit completely in a child
            QuadTree<T> child = getFittingChild(tmp);
            if(child != null){
                child.insert(obj);
            }else{
                objects.add(obj);
            }
        }
    }

    /**
     * Removes an object from this node or its child nodes.
     */
    public void remove(T obj){
        if(leaf){
            // Leaf, no children, remove from root
            objects.remove(obj, true);
        }else{
            // Remove from relevant child
            hitbox(obj);
            QuadTree<T> child = getFittingChild(tmp);

            if(child != null){
                child.remove(obj);
            }else{
                // Or root if object doesn't fit in a child
                objects.remove(obj, true);
            }

            if(getTotalObjectCount() <= maxObjectsPerNode) unsplit();
        }
    }

    /** Removes all objects. */
    public void clear(){
        objects.clear();
        if(!leaf){
            topLeft.clear();
            topRight.clear();
            botLeft.clear();
            botRight.clear();
        }
        leaf = true;
    }

    private QuadTree<T> getFittingChild(Rect boundingBox){
        float verticalMidpoint = bounds.x + (bounds.width / 2);
        float horizontalMidpoint = bounds.y + (bounds.height / 2);

        // Object can completely fit within the top quadrants
        boolean topQuadrant = boundingBox.y > horizontalMidpoint;
        // Object can completely fit within the bottom quadrants
        boolean bottomQuadrant = boundingBox.y < horizontalMidpoint && (boundingBox.y + boundingBox.height) < horizontalMidpoint;

        // Object can completely fit within the left quadrants
        if(boundingBox.x < verticalMidpoint && boundingBox.x + boundingBox.width < verticalMidpoint){
            if(topQuadrant){
                return topLeft;
            }else if(bottomQuadrant){
                return botLeft;
            }
        }else if(boundingBox.x > verticalMidpoint){ // Object can completely fit within the right quadrants
            if(topQuadrant){
                return topRight;
            }else if(bottomQuadrant){
                return botRight;
            }
        }

        // Else, object needs to be in parent cause it can't fit completely in a quadrant
        return null;
    }

    /**
     * Processes objects that may intersect the given rectangle.
     * <p>
     * This will never result in false positives.
     */
    public void intersect(float x, float y, float width, float height, Cons<T> out){
        if(!leaf){
            if(topLeft.bounds.overlaps(x, y, width, height)) topLeft.intersect(x, y, width, height, out);
            if(topRight.bounds.overlaps(x, y, width, height)) topRight.intersect(x, y, width, height, out);
            if(botLeft.bounds.overlaps(x, y, width, height)) botLeft.intersect(x, y, width, height, out);
            if(botRight.bounds.overlaps(x, y, width, height)) botRight.intersect(x, y, width, height, out);
        }

        Seq<?> objects = this.objects;

        for(int i = 0; i < objects.size; i++){
            T item = (T)objects.items[i];
            hitbox(item);
            if(tmp.overlaps(x, y, width, height)){
                out.get(item);
            }
        }
    }

    /**
     * Processes objects that may intersect the given rectangle.
     * <p>
     * This will never result in false positives.
     */
    public void intersect(Rect rect, Cons<T> out){
        intersect(rect.x, rect.y, rect.width, rect.height, out);
    }

    /**
     * Fills the out parameter with any objects that may intersect the given rectangle.
     * <p>
     * This will result in false positives, but never a false negative.
     */
    public void intersect(Rect toCheck, Seq<T> out){
        intersect(toCheck.x, toCheck.y, toCheck.width, toCheck.height, out);
    }

    /**
     * Fills the out parameter with any objects that may intersect the given rectangle.
     */
    public void intersect(float x, float y, float width, float height, Seq<T> out){
        if(!leaf){
            if(topLeft.bounds.overlaps(x, y, width, height)) topLeft.intersect(x, y, width, height, out);
            if(topRight.bounds.overlaps(x, y, width, height)) topRight.intersect(x, y, width, height, out);
            if(botLeft.bounds.overlaps(x, y, width, height)) botLeft.intersect(x, y, width, height, out);
            if(botRight.bounds.overlaps(x, y, width, height)) botRight.intersect(x, y, width, height, out);
        }

        Seq<?> objects = this.objects;

        for(int i = 0; i < objects.size; i++){
            T item = (T)objects.items[i];
            hitbox(item);
            if(tmp.overlaps(x, y, width, height)){
                out.add(item);
            }
        }
    }

    /**
     * Returns the total number of objects in this node and all child nodes, recursively
     */
    public int getTotalObjectCount(){
        int count = objects.size;
        if(!leaf){
            count += botLeft.getTotalObjectCount() + topRight.getTotalObjectCount() + topLeft.getTotalObjectCount() + botRight.getTotalObjectCount();
        }
        return count;
    }

    /** Adds all quadtree objects to the specified Seq. */
    public void getObjects(Seq<T> out){
        out.addAll(objects);

        if(!leaf){
            topLeft.getObjects(out);
            topRight.getObjects(out);
            botLeft.getObjects(out);
            botRight.getObjects(out);
        }
    }

    protected QuadTree<T> newChild(Rect rect){
        return new QuadTree<>(rect);
    }

    protected void hitbox(T t){
        t.hitbox(tmp);
    }

    /**Represents an object in a QuadTree.*/
    public interface QuadTreeObject{
        /**Fills the out parameter with this element's rough bounding box. This should never be smaller than the actual object, but may be larger.*/
        void hitbox(Rect out);
    }
}
