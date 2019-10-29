package io.anuke.arc.math.geom;

import io.anuke.arc.collection.Array;
import io.anuke.arc.func.Cons;
import io.anuke.arc.math.geom.QuadTree.QuadTreeObject;

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
@SuppressWarnings("unchecked")
public class QuadTree<T extends QuadTreeObject>{
    private static final Rectangle tmp = new Rectangle();
    private static final int maxObjectsPerNode = 5;
    private static final int botLeft = 0, botRight = 1, topRight = 2, topLeft = 3;

    private Rectangle bounds;
    private Array<T> objects = new Array<>();
    private QuadTree<T>[] children;

    public QuadTree(Rectangle bounds){
        this.bounds = bounds;
    }

    private void split(){
        if(children != null) return;

        float subW = bounds.width / 2;
        float subH = bounds.height / 2;

        children = new QuadTree[4];
        children[botLeft] = new QuadTree<>(new Rectangle(bounds.x, bounds.y, subW, subH));
        children[botRight] = new QuadTree<>(new Rectangle(bounds.x + subW, bounds.y, subW, subH));
        children[topLeft] = new QuadTree<>(new Rectangle(bounds.x, bounds.y + subH, subW, subH));
        children[topRight] = new QuadTree<>(new Rectangle(bounds.x + subW, bounds.y + subH, subW, subH));

        // Transfer objects to children if they fit entirely in one
        for(Iterator<T> iterator = objects.iterator(); iterator.hasNext(); ){
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
        if(children == null) return;

        for(int i = 0; i < 4; i++){
            objects.addAll(children[i].objects);
        }
        children = null;
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

        if(children == null && (objects.size + 1) > maxObjectsPerNode) split();

        if(children == null){
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
        if(children == null){
            // Leaf, no children, remove from root
            objects.removeValue(obj, true);
        }else{
            // Remove from relevant child
            obj.hitbox(tmp);
            QuadTree<T> child = getFittingChild(tmp);

            if(child != null){
                child.remove(obj);
            }else{
                // Or root if object doesn't fit in a child
                objects.removeValue(obj, true);
            }

            if(getTotalObjectCount() <= maxObjectsPerNode) unsplit();
        }
    }

    /** Removes all objects. */
    public void clear(){
        objects.clear();
        if(children != null){
            for(int i = 0; i < 4; i++){
                children[i].clear();
            }
        }
    }

    private QuadTree<T> getFittingChild(Rectangle boundingBox){
        float verticalMidpoint = bounds.x + (bounds.width / 2);
        float horizontalMidpoint = bounds.y + (bounds.height / 2);

        // Object can completely fit within the top quadrants
        boolean topQuadrant = boundingBox.y > horizontalMidpoint;
        // Object can completely fit within the bottom quadrants
        boolean bottomQuadrant = boundingBox.y < horizontalMidpoint && (boundingBox.y + boundingBox.height) < horizontalMidpoint;

        // Object can completely fit within the left quadrants
        if(boundingBox.x < verticalMidpoint && boundingBox.x + boundingBox.width < verticalMidpoint){
            if(topQuadrant){
                return children[topLeft];
            }else if(bottomQuadrant){
                return children[botLeft];
            }
        }
        // Object can completely fit within the right quadrants
        else if(boundingBox.x > verticalMidpoint){
            if(topQuadrant){
                return children[topRight];
            }else if(bottomQuadrant){
                return children[botRight];
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
        if(children != null){
            for(int i = 0; i < 4; i++){
                if(children[i].bounds.overlaps(x, y, width, height)){
                    children[i].getIntersect(out, x, y, width, height);
                }
            }
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
    public void getIntersect(Cons<T> out, Rectangle rect){
        getIntersect(out, rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Fills the out parameter with any objects that may intersect the given rectangle.
     * <p>
     * This will result in false positives, but never a false negative.
     */
    public void getIntersect(Array<T> out, Rectangle toCheck){
        if(children != null){
            for(int i = 0; i < 4; i++){
                if(children[i].bounds.overlaps(toCheck)){
                    children[i].getIntersect(out, toCheck);
                }
            }
        }

        out.addAll(objects);
    }

    /**
     * Returns whether this node is a leaf node (has no child nodes)
     */
    public boolean isLeaf(){
        return children == null;
    }

    /**
     * Returns the bottom left child node, or null if this node is a leaf node.
     */
    public QuadTree<T> getBottomLeftChild(){
        return children[botLeft];
    }

    /**
     * Returns the bottom right child node, or null if this node is a leaf node.
     */
    public QuadTree<T> getBottomRightChild(){
        return children[botRight];
    }

    /**
     * Returns the top left child node, or null if this node is a leaf node.
     */
    public QuadTree<T> getTopLeftChild(){
        return children[topLeft];
    }

    /**
     * Returns the top right child node, or null if this node is a leaf node.
     */
    public QuadTree<T> getTopRightChild(){
        return children[topRight];
    }

    /**
     * Returns the entire bounds of this node.
     */
    public Rectangle getBounds(){
        return bounds;
    }

    /**
     * Returns the objects in this node only.
     * <p>
     * If this node isn't a leaf node, it will only return the objects that don't fit perfectly into a specific child node (lie on a border).
     */
    public Array<T> getObjects(){
        return objects;
    }

    /**
     * Returns the total number of objects in this node and all child nodes, recursively
     */
    public int getTotalObjectCount(){
        int count = objects.size;
        if(children != null){
            for(int i = 0; i < 4; i++){
                count += children[i].getTotalObjectCount();
            }
        }
        return count;
    }

    /**
     * Fills the out array with all objects in this node and all child nodes, recursively.
     */
    public void getAllChildren(Array<T> out){
        out.addAll(objects);

        if(children != null){
            for(int i = 0; i < 4; i++){
                children[i].getAllChildren(out);
            }
        }
    }

    /**Represents an object in a QuadTree.*/
    public interface QuadTreeObject{
        /**Fills the out parameter with this element's rough bounding box. This should never be smaller than the actual object, but may be larger.*/
        void hitbox(Rectangle out);
    }
}
