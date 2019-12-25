package arc.backend.teavm;

import arc.input.*;
import org.teavm.jso.dom.events.*;

public class TeaKeymap{
    public static KeyCode getButton(int button){
        switch(button){
            case MouseEvent.RIGHT_BUTTON: return KeyCode.MOUSE_RIGHT;
            case MouseEvent.MIDDLE_BUTTON: return KeyCode.MOUSE_MIDDLE;
            default: return KeyCode.MOUSE_LEFT;
        }
    }

    public static KeyCode getCode(int keyCode){
        switch(keyCode){
            case 18: return KeyCode.ALT_LEFT;
            case 8: return KeyCode.BACKSPACE;
            case 17: return KeyCode.CONTROL_LEFT;
            case 46: return KeyCode.DEL;
            case 40: return KeyCode.DOWN;
            case 35: return KeyCode.END;
            case 13: return KeyCode.ENTER;
            case 27: return KeyCode.ESCAPE;
            case 36: return KeyCode.HOME;
            case 37: return KeyCode.LEFT;
            case 34: return KeyCode.PAGE_DOWN;
            case 33: return KeyCode.PAGE_UP;
            case 39: return KeyCode.RIGHT;
            case 16: return KeyCode.SHIFT_LEFT;
            case 9: return KeyCode.TAB;
            case 38: return KeyCode.UP;
            case 19: return KeyCode.UNKNOWN; // FIXME
            case 20: return KeyCode.UNKNOWN; // FIXME
            case 32: return KeyCode.SPACE;
            case 45: return KeyCode.INSERT;
            case 48: return KeyCode.NUM_0;
            case 49: return KeyCode.NUM_1;
            case 50: return KeyCode.NUM_2;
            case 51: return KeyCode.NUM_3;
            case 52: return KeyCode.NUM_4;
            case 53: return KeyCode.NUM_5;
            case 54: return KeyCode.NUM_6;
            case 55: return KeyCode.NUM_7;
            case 56: return KeyCode.NUM_8;
            case 57: return KeyCode.NUM_9;
            case 65: return KeyCode.A;
            case 66: return KeyCode.B;
            case 67: return KeyCode.C;
            case 68: return KeyCode.D;
            case 69: return KeyCode.E;
            case 70: return KeyCode.F;
            case 71: return KeyCode.G;
            case 72: return KeyCode.H;
            case 73: return KeyCode.I;
            case 74: return KeyCode.J;
            case 75: return KeyCode.K;
            case 76: return KeyCode.L;
            case 77: return KeyCode.M;
            case 78: return KeyCode.N;
            case 79: return KeyCode.O;
            case 80: return KeyCode.P;
            case 81: return KeyCode.Q;
            case 82: return KeyCode.R;
            case 83: return KeyCode.S;
            case 84: return KeyCode.T;
            case 85: return KeyCode.U;
            case 86: return KeyCode.V;
            case 87: return KeyCode.W;
            case 88: return KeyCode.X;
            case 89: return KeyCode.Y;
            case 90: return KeyCode.Z;
            case 91: return KeyCode.UNKNOWN; // FIXME
            case 92: return KeyCode.UNKNOWN; // FIXME
            case 96: return KeyCode.NUMPAD_0;
            case 97: return KeyCode.NUMPAD_1;
            case 98: return KeyCode.NUMPAD_2;
            case 99: return KeyCode.NUMPAD_3;
            case 100: return KeyCode.NUMPAD_4;
            case 101: return KeyCode.NUMPAD_5;
            case 102: return KeyCode.NUMPAD_6;
            case 103: return KeyCode.NUMPAD_7;
            case 104: return KeyCode.NUMPAD_8;
            case 105: return KeyCode.NUMPAD_9;
            case 106: return KeyCode.UNKNOWN; // FIXME
            case 107: return KeyCode.PLUS;
            case 109: return KeyCode.MINUS;
            case 110: return KeyCode.PERIOD;
            case 111: return KeyCode.UNKNOWN; // FIXME
            case 112: return KeyCode.F1;
            case 113: return KeyCode.F2;
            case 114: return KeyCode.F3;
            case 115: return KeyCode.F4;
            case 116: return KeyCode.F5;
            case 117: return KeyCode.F6;
            case 118: return KeyCode.F7;
            case 119: return KeyCode.F8;
            case 120: return KeyCode.F9;
            case 121: return KeyCode.F10;
            case 122: return KeyCode.F11;
            case 123: return KeyCode.F12;
            case 144: return KeyCode.NUM;
            case 145: return KeyCode.UNKNOWN; // FIXME
            case 186: return KeyCode.SEMICOLON;
            case 187: return KeyCode.EQUALS;
            case 188: return KeyCode.COMMA;
            case 189: return KeyCode.MINUS;
            case 190: return KeyCode.PERIOD;
            case 191: return KeyCode.SLASH;
            case 192: return KeyCode.BACKTICK;
            case 219: return KeyCode.LEFT_BRACKET;
            case 220: return KeyCode.BACKSLASH;
            case 221: return KeyCode.RIGHT_BRACKET;
            case 222: return KeyCode.APOSTROPHE;
            default: return KeyCode.UNKNOWN;
        }
    }

}
