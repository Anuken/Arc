package arc.graphics.vector;

interface PathConstants{
    int moveTo = 0;
    int lineTo = 1;
    int cubicTo = 2;
    int close = 3;
    int winding = 4;

    int PT_CORNER = 0x01;
    int PT_LEFT = 0x02;
    int PT_BEVEL = 0x04;
    int PT_INNERBEVEL = 0x08;
    int PT_FILL_BEVEL = 0x10;
    int PT_FILL_INNERBEVEL = 0x20;
}
