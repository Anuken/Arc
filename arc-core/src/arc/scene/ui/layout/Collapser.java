package arc.scene.ui.layout;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.util.*;

public class Collapser extends WidgetGroup{
    private Table table;
    private CollapseAction collapseAction = new CollapseAction();
    private boolean collapsed;
    private boolean actionRunning;
    private float currentHeight;
    private float seconds = 0.4f;

    public Collapser(Cons<Table> cons, boolean collapsed){
        this(new Table(), collapsed);
        cons.get(table);
    }

    public Collapser(Table table, boolean collapsed){
        this.table = table;
        this.collapsed = collapsed;
        setTransform(true);

        updateTouchable();
        addChild(table);
    }

    public Collapser setDuration(float seconds){
        this.seconds = seconds;
        return this;
    }

    public void toggle(){
        setCollapsed(!isCollapsed());
    }

    public void toggle(boolean animated){
        setCollapsed(!isCollapsed(), animated);
    }

    public void setCollapsed(boolean collapse, boolean withAnimation){
        this.collapsed = collapse;
        updateTouchable();

        if(table == null) return;

        actionRunning = true;

        if(withAnimation){
            addAction(collapseAction);
        }else{
            if(collapse){
                currentHeight = 0;
                collapsed = true;
            }else{
                currentHeight = table.getPrefHeight();
                collapsed = false;
            }

            actionRunning = false;
            invalidateHierarchy();
        }
    }

    public void setCollapsed(boolean collapse){
        setCollapsed(collapse, true);
    }

    public boolean isCollapsed(){
        return collapsed;
    }

    private void updateTouchable(){
        touchable(collapsed ? Touchable.disabled : Touchable.enabled);
    }

    @Override
    public void draw(){
        if(currentHeight > 1){
            Draw.flush();
            if(clipBegin(getX(), getY(), getWidth(), currentHeight)){
                super.draw();
                Draw.flush();
                clipEnd();
            }
        }
    }

    @Override
    public void layout(){
        if(table == null) return;

        table.setBounds(0, 0, getWidth(), getHeight());

        if(!actionRunning){
            if(collapsed)
                currentHeight = 0;
            else
                currentHeight = table.getPrefHeight();
        }
    }

    @Override
    public float getPrefWidth(){
        return table == null ? 0 : table.getPrefWidth();
    }

    @Override
    public float getPrefHeight(){
        if(table == null) return 0;

        if(!actionRunning){
            if(collapsed)
                return 0;
            else
                return table.getPrefHeight();
        }

        return currentHeight;
    }

    public void setTable(Table table){
        this.table = table;
        clearChildren();
        addChild(table);
    }

    @Override
    public float getMinWidth(){
        return 0;
    }

    @Override
    public float getMinHeight(){
        return 0;
    }

    @Override
    protected void childrenChanged(){
        super.childrenChanged();
        if(getChildren().size > 1) throw new ArcRuntimeException("Only one actor can be added to CollapsibleWidget");
    }

    private class CollapseAction extends Action{
        @Override
        public boolean act(float delta){
            if(collapsed){
                currentHeight -= delta * table.getPrefHeight() / seconds;
                if(currentHeight <= 0){
                    currentHeight = 0;
                    actionRunning = false;
                }
            }else{
                currentHeight += delta * table.getPrefHeight() / seconds;
                if(currentHeight > table.getPrefHeight()){
                    currentHeight = table.getPrefHeight();
                    actionRunning = false;
                }
            }

            invalidateHierarchy();
            return !actionRunning;
        }
    }
}