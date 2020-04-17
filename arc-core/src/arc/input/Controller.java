package arc.input;

import arc.math.geom.*;

public abstract class Controller extends InputDevice{

    public Vec3 accelerometer(){
        return Vec3.Zero;
    }

    public abstract int index();

    @Override
    public void postUpdate(){

    }

    @Override
    public DeviceType type(){
        return DeviceType.controller;
    }
}
