package arc.graphics.vector;

import arc.struct.*;

public class CanvasFlags{
    private Bits flags = new Bits(3);

    public void and(CanvasFlag... flags){
        this.flags.clear();
        for(CanvasFlag flag : flags){
            this.flags.set(flag.ordinal());
        }
    }

    public void set(CanvasFlag... flags){
        for(CanvasFlag flag : flags){
            this.flags.set(flag.ordinal());
        }
    }

    public void clear(CanvasFlag... flags){
        for(CanvasFlag flag : flags){
            this.flags.clear(flag.ordinal());
        }
    }

    public void clear(){
        this.flags.clear();
    }

    public boolean isAntiAlias(){
        return this.flags.get(CanvasFlag.antiAlias.ordinal());
    }

    public void setAntiAlias(boolean antialias){
        if(antialias){
            this.flags.set(CanvasFlag.antiAlias.ordinal());
        }else{
            this.flags.clear(CanvasFlag.antiAlias.ordinal());
        }
    }

    public boolean isStencilStrokes(){
        return this.flags.get(CanvasFlag.stencilStrokes.ordinal());
    }

    public void setStencilStrokes(boolean stencilStrokes){
        if(stencilStrokes){
            this.flags.set(CanvasFlag.stencilStrokes.ordinal());
        }else{
            this.flags.clear(CanvasFlag.stencilStrokes.ordinal());
        }
    }

    public boolean isDebug(){
        return this.flags.get(CanvasFlag.debug.ordinal());
    }

    public void setDebug(boolean debug){
        if(debug){
            this.flags.set(CanvasFlag.debug.ordinal());
        }else{
            this.flags.clear(CanvasFlag.debug.ordinal());
        }
    }

    public enum CanvasFlag{
        antiAlias, stencilStrokes, debug
    }
}
