package arc.math.geom;

import arc.struct.Array;
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
    private static final Rect tmp = new Rect();
    private static final int maxObjectsPerNode = 5;

    public Rect bounds;
    public Array<T> objects = new Array<>();
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
            botLeft = new QuadTree<>(new Rect(bounds.x, bounds.y, subW, subH));
            botRight = new QuadTree<>(new Rect(bounds.x + subW, bounds.y, subW, subH));
            topLeft = new QuadTree<>(new Rect(bounds.x, bounds.y + subH, subW, subH));
            topRight = new QuadTree<>(new Rect(bounds.x + subW, bounds.y + subH, subW, subH));
        }
        leaf = false;

        // Transfer objects to children if they fit entirely in one
        for(Iterator<T> iterator = objects.iterator(); iterator.hasNext();){
            T obj = iterator.next();
            obj.hitbox(tmp);
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
        leaf = true;
    }

    /**
     * Inserts an object into this node or its child nodes. This will split a leaf node if it exceeds the object limit.
     */
    public void insert(T obj){
        obj.hitbox(tmp);
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
            obj.hitbox(tmp);
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
            obj.hitbox(tmp);
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
    public void getIntersect(Cons<T> out, float x, float y, float width, float height){
        if(!leaf){
            if(topLeft.bounds.overlaps(x, y, width, height)) topLeft.getIntersect(out, x, y, width, height);
            if(topRight.bounds.overlaps(x, y, width, height)) topRight.getIntersect(out, x, y, width, height);
            if(botLeft.bounds.overlaps(x, y, width, height)) botLeft.getIntersect(out, x, y, width, height);
            if(botRight.bounds.overlaps(x, y, width, height)) botRight.getIntersect(out, x, y, width, height);
        }

        for(int i = 0; i < objects.size; i++){
            objects.get(i).hitbox(tmp);
            if(tmp.overlaps(x, y, width, height)){
                out.get(objects.get(i));
            }
        }
    }

    /**
     * Processes objects that may intersect the given rectangle.
     * <p>
     * This will never result in false positives.
     */
    public void getIntersect(Cons<T> out, Rect rect){
        getIntersect(out, rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Fills the out parameter with any objects that may intersect the given rectangle.
     * <p>
     * This will result in false positives, but never a false negative.
     */
    public void getIntersect(Array<T> out, Rect toCheck){
        if(!leaf){
            if(topLeft.bounds.overlaps(toCheck)) topLeft.getIntersect(out, toCheck);
            if(topRight.bounds.overlaps(toCheck)) topRight.getIntersect(out, toCheck);
            if(botLeft.bounds.overlaps(toCheck)) botLeft.getIntersect(out, toCheck);
            if(botRight.bounds.overlaps(toCheck)) botRight.getIntersect(out, toCheck);
        }

        for(int i = 0; i < objects.size; i++){
            objects.get(i).hitbox(tmp);
            if(tmp.overlaps(toCheck)){
                out.add(objects.get(i));
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

    /**Represents an object in a QuadTree.*/
    public interface QuadTreeObject{
        /**Fills the out parameter with this element's rough bounding box. This should never be smaller than the actual object, but may be larger.*/
        void hitbox(Rect out);
    }
}
