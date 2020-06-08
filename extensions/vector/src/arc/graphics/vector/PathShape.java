package arc.graphics.vector;

import arc.math.*;
import arc.struct.*;
import arc.util.pooling.*;

public class PathShape extends Shape{
    private Seq<PathCommand> commands = new Seq<>();

    public PathShape arcTo(float startX, float startY, float endX, float endY, float radius){
        return arcTo(false, startX, startY, endX, endY, radius);
    }

    public PathShape arcToRel(float startX, float startY, float endX, float endY, float radius){
        return arcTo(true, startX, startY, endX, endY, radius);
    }

    public PathShape arcTo(boolean relative, float startX, float startY, float endX, float endY, float radius){
        ArcToCommand command = Pools.obtain(ArcToCommand.class, ArcToCommand::new);
        command.relative = relative;
        command.startX = startX;
        command.startY = startY;
        command.endX = endX;
        command.endY = endY;
        command.radius = radius;
        commands.add(command);
        return this;
    }

    public PathShape arcTo(float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag, boolean sweepFlag, float x, float y){
        return arcTo(false, radiusX, radiusY, angleDegrees, largeArcFlag, sweepFlag, x, y);
    }

    public PathShape arcToRel(float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag, boolean sweepFlag, float x, float y){
        return arcTo(true, radiusX, radiusY, angleDegrees, largeArcFlag, sweepFlag, x, y);
    }

    public PathShape arcToRad(float radiusX, float radiusY, float angleRadians, boolean largeArcFlag, boolean sweepFlag, float x, float y){
        return arcTo(false, radiusX, radiusY, Mathf.radiansToDegrees * angleRadians, largeArcFlag, sweepFlag, x, y);
    }

    public PathShape arcToRadRel(float radiusX, float radiusY, float angleRadians, boolean largeArcFlag, boolean sweepFlag, float x, float y){
        return arcTo(true, radiusX, radiusY, Mathf.radiansToDegrees * angleRadians, largeArcFlag, sweepFlag, x, y);
    }

    public PathShape arcTo(boolean relative, float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag, boolean sweepFlag, float x, float y){
        SvgArcToCommand command = Pools.obtain(SvgArcToCommand.class, SvgArcToCommand::new);
        command.relative = relative;
        command.radiusX = radiusX;
        command.radiusY = radiusY;
        command.angleDegrees = angleDegrees;
        command.largeArcFlag = largeArcFlag;
        command.sweepFlag = sweepFlag;
        command.x = x;
        command.y = y;
        commands.add(command);
        return this;
    }

    public PathShape moveTo(float x, float y){
        return moveTo(false, x, y);
    }

    public PathShape moveToRel(float x, float y){
        return moveTo(true, x, y);
    }

    public PathShape moveTo(boolean relative, float x, float y){
        MoveToCommand command = Pools.obtain(MoveToCommand.class, MoveToCommand::new);
        command.relative = relative;
        command.x = x;
        command.y = y;
        commands.add(command);
        return this;
    }

    public PathShape verticalMoveTo(float y){
        return verticalMoveTo(false, y);
    }

    public PathShape verticalMoveToRel(float y){
        return verticalMoveTo(true, y);
    }

    public PathShape verticalMoveTo(boolean relative, float y){
        VerticalMoveToCommand command = Pools.obtain(VerticalMoveToCommand.class, VerticalMoveToCommand::new);
        command.relative = relative;
        command.y = y;
        commands.add(command);
        return this;
    }

    public PathShape horizontalMoveTo(float x){
        return horizontalMoveTo(false, x);
    }


    public PathShape horizontalMoveToRel(float x){
        return horizontalMoveTo(true, x);
    }

    public PathShape horizontalMoveTo(boolean relative, float x){
        HorizontalMoveToCommand command = Pools.obtain(HorizontalMoveToCommand.class, HorizontalMoveToCommand::new);
        command.relative = relative;
        command.x = x;
        commands.add(command);
        return this;
    }

    public PathShape lineTo(float x, float y){
        return lineTo(false, x, y);
    }

    public PathShape lineToRel(float x, float y){
        return lineTo(true, x, y);
    }

    private PathShape lineTo(boolean relative, float x, float y){
        LineToCommand command = Pools.obtain(LineToCommand.class, LineToCommand::new);
        command.relative = relative;
        command.x = x;
        command.y = y;
        commands.add(command);
        return this;
    }

    public PathShape verticalLineTo(float y){
        return verticalLineTo(false, y);
    }

    public PathShape verticalLineToRel(float y){
        return verticalLineTo(true, y);
    }

    public PathShape verticalLineTo(boolean relative, float y){
        VerticalLineToCommand command = Pools.obtain(VerticalLineToCommand.class, VerticalLineToCommand::new);
        command.relative = relative;
        command.y = y;
        commands.add(command);
        return this;
    }

    public PathShape horizontalLineTo(float x){
        return horizontalLineTo(false, x);
    }


    public PathShape horizontalLineToRel(float x){
        return horizontalLineTo(true, x);
    }

    public PathShape horizontalLineTo(boolean relative, float x){
        HorizontalLineToCommand command = Pools.obtain(HorizontalLineToCommand.class, HorizontalLineToCommand::new);
        command.relative = relative;
        command.x = x;
        commands.add(command);
        return this;
    }

    public PathShape quadTo(float controlX, float controlY, float x, float y){
        return quadTo(false, controlX, controlY, x, y);
    }

    public PathShape quadToRel(float controlX, float controlY, float x, float y){
        return quadTo(true, controlX, controlY, x, y);
    }

    public PathShape quadTo(boolean relative, float controlX, float controlY, float x, float y){
        QuadToCommand command = Pools.obtain(QuadToCommand.class, QuadToCommand::new);
        command.relative = relative;
        command.controlX = controlX;
        command.controlY = controlY;
        command.x = x;
        command.y = y;
        commands.add(command);
        return this;
    }

    public PathShape quadSmoothTo(float x, float y){
        return quadSmoothTo(false, x, y);
    }

    public PathShape quadSmoothToRel(float x, float y){
        return quadSmoothTo(true, x, y);
    }

    public PathShape quadSmoothTo(boolean relative, float x, float y){
        QuadSmoothToCommand command = Pools.obtain(QuadSmoothToCommand.class, QuadSmoothToCommand::new);
        command.relative = relative;
        command.x = x;
        command.y = y;
        commands.add(command);
        return this;
    }

    public PathShape cubicTo(float controlX1, float controlY1, float controlX2, float controlY2, float x, float y){
        return cubicTo(false, controlX1, controlY1, controlX2, controlY2, x, y);
    }

    public PathShape cubicToRel(float controlX1, float controlY1, float controlX2, float controlY2, float x, float y){
        return cubicTo(true, controlX1, controlY1, controlX2, controlY2, x, y);
    }

    public PathShape cubicTo(boolean relative, float controlX1, float controlY1, float controlX2, float controlY2, float x, float y){
        CubicToCommand command = Pools.obtain(CubicToCommand.class, CubicToCommand::new);
        command.relative = relative;
        command.controlX1 = controlX1;
        command.controlY1 = controlY1;
        command.controlX2 = controlX2;
        command.controlY2 = controlY2;
        command.x = x;
        command.y = y;
        commands.add(command);
        return this;
    }

    public PathShape cubicSmoothTo(float controlX2, float controlY2, float x, float y){
        return cubicSmoothTo(false, controlX2, controlY2, x, y);
    }

    public PathShape cubicSmoothToRel(float controlX2, float controlY2, float x, float y){
        return cubicSmoothTo(true, controlX2, controlY2, x, y);
    }

    public PathShape cubicSmoothTo(boolean relative, float controlX2, float controlY2, float x, float y){
        CubicSmoothToCommand command = Pools.obtain(CubicSmoothToCommand.class, CubicSmoothToCommand::new);
        command.relative = relative;
        command.controlX2 = controlX2;
        command.controlY2 = controlY2;
        command.x = x;
        command.y = y;
        commands.add(command);
        return this;
    }

    public PathShape closePath(){
        commands.add(Pools.obtain(CloseCommand.class, CloseCommand::new));
        return this;
    }

    public PathShape winding(Winding winding){
        WindingCommand command = Pools.obtain(WindingCommand.class, WindingCommand::new);
        command.winding = winding;
        commands.add(command);
        return this;
    }

    @Override
    public void reset(){
        super.reset();
        CanvasUtils.resetArray(commands);
    }

    @Override
    protected void initPath(Path path){
        for(int i = 0; i < commands.size; i++){
            PathCommand command = commands.get(i);
            command.appendPath(path);
        }
    }

    private static abstract class PathCommand{
        protected boolean relative;

        protected abstract void appendPath(Path path);
    }

    private static class SvgArcToCommand extends PathCommand{
        private float radiusX;
        private float radiusY;
        private float angleDegrees;
        private boolean largeArcFlag;
        private boolean sweepFlag;
        private float x;
        private float y;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.arcToRel(radiusX, radiusY, angleDegrees, largeArcFlag, sweepFlag, radiusX, radiusY);
            }else{
                path.arcTo(radiusX, radiusY, angleDegrees, largeArcFlag, sweepFlag, radiusX, radiusY);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "a " : "A ";
            return command + radiusX + " " + radiusY + " " + angleDegrees + " " + largeArcFlag
            + " " + sweepFlag + " " + x + " " + y;
        }
    }

    private static class ArcToCommand extends PathCommand{
        private float startX;
        private float startY;
        private float endX;
        private float endY;
        private float radius;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.arcToRel(startX, startY, endX, endY, radius);
            }else{
                path.arcTo(startX, startY, endX, endY, radius);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "a " : "A ";
            return command + startX + " " + startY + " " + endX + " " + endY + " " + radius;
        }
    }

    private static class CubicToCommand extends PathCommand{
        private float controlX1;
        private float controlY1;
        private float controlX2;
        private float controlY2;
        private float x;
        private float y;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y);
            }else{
                path.cubicToRel(controlX1, controlY1, controlX2, controlY2, x, y);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "c " : "C ";
            return command + controlX1 + " " + controlY1 + " " + controlX2 + " " + controlY2 + " " + x + " " + y;
        }
    }

    private static class CubicSmoothToCommand extends PathCommand{
        float x;
        float y;
        private float controlX2;
        private float controlY2;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.cubicSmoothTo(controlX2, controlY2, x, y);
            }else{
                path.cubicSmoothToRel(controlX2, controlY2, x, y);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "s " : "S ";
            return command + " " + controlX2 + " " + controlY2 + " " + x + " " + y;
        }
    }

    private static class QuadToCommand extends PathCommand{
        private float controlX;
        private float controlY;
        private float x;
        private float y;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.quadToRel(controlX, controlY, x, y);
            }else{
                path.quadTo(controlX, controlY, x, y);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "q " : "Q ";
            return command + controlX + " " + controlY + " " + x + " " + y;
        }
    }

    private static class QuadSmoothToCommand extends PathCommand{
        float x;
        float y;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.quadSmoothToRel(x, y);
            }else{
                path.quadSmoothTo(x, y);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "t " : "T ";
            return command + x + " " + y;
        }
    }

    private static class MoveToCommand extends PathCommand{
        float x;
        float y;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.moveToRel(x, y);
            }else{
                path.moveTo(x, y);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "m " : "M ";
            return command + x + " " + y;
        }
    }

    private static class VerticalMoveToCommand extends PathCommand{
        float y;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.verticalMoveToRel(y);
            }else{
                path.verticalMoveTo(y);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "vm " : "VM ";
            return command + y;
        }
    }

    private static class HorizontalMoveToCommand extends PathCommand{
        float x;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.horizontalMoveToRel(x);
            }else{
                path.horizontalMoveTo(x);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "hm " : "HM ";
            return command + x;
        }
    }

    private static class CloseCommand extends PathCommand{
        @Override
        protected void appendPath(Path path){
            path.close();
        }

        @Override
        public String toString(){
            return "Z";
        }
    }

    private static class LineToCommand extends PathCommand{
        float x;
        float y;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.lineToRel(x, y);
            }else{
                path.lineTo(x, y);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "l " : "L ";
            return command + x + " " + y;
        }
    }

    private static class VerticalLineToCommand extends PathCommand{
        float y;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.verticalLineToRel(y);
            }else{
                path.verticalLineTo(y);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "v " : "V ";
            return command + y;
        }
    }

    private static class HorizontalLineToCommand extends PathCommand{
        float x;

        @Override
        protected void appendPath(Path path){
            if(relative){
                path.horizontalLineToRel(x);
            }else{
                path.horizontalLineTo(x);
            }
        }

        @Override
        public String toString(){
            String command = relative ? "h " : "H ";
            return command + x;
        }
    }

    private static class WindingCommand extends PathCommand{
        Winding winding;

        @Override
        protected void appendPath(Path path){
            path.winding(winding);
        }

        @Override
        public String toString(){
            return "W " + winding.name();
        }
    }
}
