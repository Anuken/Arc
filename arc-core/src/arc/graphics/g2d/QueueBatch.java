package arc.graphics.g2d;

import arc.graphics.*;

/** Stores sprites and draws them with a delay. */
public class QueueBatch extends SortedSpriteBatch{

    public QueueBatch(){
        //this makes requests get queued
        this.sort = true;
    }

    //blending should not flush the batch
    @Override
    public void setBlending(Blending blending){
        this.blending = blending;
    }

    @Override
    public void flush(){
        super.flush();
    }

    @Override
    protected void setSort(boolean sort){
        //does nothing
    }

    @Override
    protected void sortRequests(){
        //does nothing, as sorting isn't needed here
    }
}
