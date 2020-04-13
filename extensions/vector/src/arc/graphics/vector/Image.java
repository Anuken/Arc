package arc.graphics.vector;

import arc.graphics.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class Image implements Poolable{
    int id;
    Texture texture;
    boolean flipX;
    boolean flipY;
    boolean premultiplied;
    boolean canvasManaged;

    public static Image obtain(Texture texture, boolean flipX, boolean flipY, boolean premultiplied){
        return obtain(0, texture, flipX, flipY, premultiplied, false);
    }

    static Image obtain(int id, Texture texture, boolean flipX, boolean flipY, boolean premultiplied, boolean canvasManaged){
        Image image = Pools.obtain(Image.class, Image::new);
        image.id = id;
        image.texture = texture;
        image.flipX = flipX;
        image.flipY = flipY;
        image.premultiplied = premultiplied;
        image.canvasManaged = canvasManaged;
        return image;
    }

    @Override
    public void reset(){
        if(canvasManaged){
            texture.dispose();
        }

        id = 0;
        flipX = false;
        flipY = false;
        texture = null;
        premultiplied = false;
        canvasManaged = false;
    }

    public void free(){
        Pools.free(this);
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + (flipX ? 1231 : 1237);
        result = prime * result + (flipY ? 1231 : 1237);
        result = prime * result + (premultiplied ? 1231 : 1237);
        result = prime * result + texture.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        Image other = (Image)obj;
        if(flipX != other.flipX){
            return false;
        }
        if(flipY != other.flipY){
            return false;
        }
        if(premultiplied != other.premultiplied){
            return false;
        }
        return texture.equals(other.texture);
    }

    @Override
    public String toString(){
        return "Image [id=" + id + ", texture=" + texture + ", flipX=" + flipX + ", flipY=" + flipY + ", premultiplied=" + premultiplied
        + ", canvasManaged=" + canvasManaged + "]";
    }
}
