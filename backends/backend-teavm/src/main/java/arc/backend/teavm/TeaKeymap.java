package arc.backend.teavm;

import arc.input.*;
import org.teavm.jso.dom.events.*;

public class TeaKeymap{
    public static KeyCode getButton(int button){
        switch(button){
            case MouseEvent.RIGHT_BUTTON: return KeyCode.mouseRight;
            case MouseEvent.MIDDLE_BUTTON: return KeyCode.mouseMiddle;
            default: return KeyCode.mouseLeft;
        }
    }

    public static KeyCode getCode(int keyCode){
        switch(keyCode){
            case 18: return KeyCode.altLeft;
            case 8: return KeyCode.backspace;
            case 17: return KeyCode.controlLeft;
            case 46: return KeyCode.del;
            case 40: return KeyCode.down;
            case 35: return KeyCode.end;
            case 13: return KeyCode.enter;
            case 27: return KeyCode.escape;
            case 36: return KeyCode.home;
            case 37: return KeyCode.left;
            case 34: return KeyCode.pageDown;
            case 33: return KeyCode.pageUp;
            case 39: return KeyCode.right;
            case 16: return KeyCode.shiftLeft;
            case 9: return KeyCode.tab;
            case 38: return KeyCode.up;
            case 19: return KeyCode.unknown; // FIXME
            case 20: return KeyCode.unknown; // FIXME
            case 32: return KeyCode.space;
            case 45: return KeyCode.insert;
            case 48: return KeyCode.num0;
            case 49: return KeyCode.num1;
            case 50: return KeyCode.num2;
            case 51: return KeyCode.num3;
            case 52: return KeyCode.num4;
            case 53: return KeyCode.num5;
            case 54: return KeyCode.num6;
            case 55: return KeyCode.num7;
            case 56: return KeyCode.num8;
            case 57: return KeyCode.num9;
            case 65: return KeyCode.a;
            case 66: return KeyCode.b;
            case 67: return KeyCode.c;
            case 68: return KeyCode.d;
            case 69: return KeyCode.e;
            case 70: return KeyCode.f;
            case 71: return KeyCode.g;
            case 72: return KeyCode.h;
            case 73: return KeyCode.i;
            case 74: return KeyCode.j;
            case 75: return KeyCode.k;
            case 76: return KeyCode.l;
            case 77: return KeyCode.m;
            case 78: return KeyCode.n;
            case 79: return KeyCode.o;
            case 80: return KeyCode.p;
            case 81: return KeyCode.q;
            case 82: return KeyCode.r;
            case 83: return KeyCode.s;
            case 84: return KeyCode.t;
            case 85: return KeyCode.u;
            case 86: return KeyCode.v;
            case 87: return KeyCode.w;
            case 88: return KeyCode.x;
            case 89: return KeyCode.y;
            case 90: return KeyCode.z;
            case 91: return KeyCode.unknown; // FIXME
            case 92: return KeyCode.unknown; // FIXME
            case 96: return KeyCode.numpad0;
            case 97: return KeyCode.numpad1;
            case 98: return KeyCode.numpad2;
            case 99: return KeyCode.numpad3;
            case 100: return KeyCode.numpad4;
            case 101: return KeyCode.numpad5;
            case 102: return KeyCode.numpad6;
            case 103: return KeyCode.numpad7;
            case 104: return KeyCode.numpad8;
            case 105: return KeyCode.numpad9;
            case 106: return KeyCode.unknown; // FIXME
            case 107: return KeyCode.plus;
            case 109: return KeyCode.minus;
            case 110: return KeyCode.period;
            case 111: return KeyCode.unknown; // FIXME
            case 112: return KeyCode.f1;
            case 113: return KeyCode.f2;
            case 114: return KeyCode.f3;
            case 115: return KeyCode.f4;
            case 116: return KeyCode.f5;
            case 117: return KeyCode.f6;
            case 118: return KeyCode.f7;
            case 119: return KeyCode.f8;
            case 120: return KeyCode.f9;
            case 121: return KeyCode.f10;
            case 122: return KeyCode.f11;
            case 123: return KeyCode.f12;
            case 144: return KeyCode.num;
            case 145: return KeyCode.unknown; // FIXME
            case 186: return KeyCode.semicolon;
            case 187: return KeyCode.equals;
            case 188: return KeyCode.comma;
            case 189: return KeyCode.minus;
            case 190: return KeyCode.period;
            case 191: return KeyCode.slash;
            case 192: return KeyCode.backtick;
            case 219: return KeyCode.leftBracket;
            case 220: return KeyCode.backslash;
            case 221: return KeyCode.rightBracket;
            case 222: return KeyCode.apostrophe;
            default: return KeyCode.unknown;
        }
    }

}
