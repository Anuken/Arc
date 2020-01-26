package arc.backend.sdl;

import arc.input.*;

public class SdlScanmap{

    public static KeyCode getCode(int code){
        switch(code){
            case 0: return KeyCode.UNKNOWN;
            case 42: return KeyCode.BACKSPACE;
            case 43: return KeyCode.TAB;
            case 88:
            case 40: return KeyCode.ENTER;
            case 41: return KeyCode.ESCAPE;
            case 44: return KeyCode.SPACE;
            case 87: return KeyCode.PLUS;
            case 54:
            case 133: return KeyCode.COMMA;
            case 45:
            case 86: return KeyCode.MINUS;
            case 55:
            case 99: return KeyCode.PERIOD;
            case 56:
            case 84: return KeyCode.SLASH;
            case 85: return KeyCode.ASTERISK;
            case 98: return KeyCode.NUMPAD_0;
            case 89: return KeyCode.NUMPAD_1;
            case 90: return KeyCode.NUMPAD_2;
            case 91: return KeyCode.NUMPAD_3;
            case 92: return KeyCode.NUMPAD_4;
            case 93: return KeyCode.NUMPAD_5;
            case 94: return KeyCode.NUMPAD_6;
            case 95: return KeyCode.NUMPAD_7;
            case 96: return KeyCode.NUMPAD_8;
            case 97: return KeyCode.NUMPAD_9;
            case 39: return KeyCode.NUM_0;
            case 30: return KeyCode.NUM_1;
            case 31: return KeyCode.NUM_2;
            case 32: return KeyCode.NUM_3;
            case 33: return KeyCode.NUM_4;
            case 34: return KeyCode.NUM_5;
            case 35: return KeyCode.NUM_6;
            case 36: return KeyCode.NUM_7;
            case 37: return KeyCode.NUM_8;
            case 38: return KeyCode.NUM_9;
            case 203: return KeyCode.COLON;
            case 51: return KeyCode.SEMICOLON;
            case 52: return KeyCode.APOSTROPHE;
            case 46: return KeyCode.EQUALS;
            case 206: return KeyCode.AT;
            case 47: return KeyCode.LEFT_BRACKET;
            case 48: return KeyCode.RIGHT_BRACKET;
            case 49: return KeyCode.BACKSLASH;
            case 53: return KeyCode.BACKTICK;
            case 4: return KeyCode.A;
            case 5: return KeyCode.B;
            case 6: return KeyCode.C;
            case 7: return KeyCode.D;
            case 8: return KeyCode.E;
            case 9: return KeyCode.F;
            case 10: return KeyCode.G;
            case 11: return KeyCode.H;
            case 12: return KeyCode.I;
            case 13: return KeyCode.J;
            case 14: return KeyCode.K;
            case 15: return KeyCode.L;
            case 16: return KeyCode.M;
            case 17: return KeyCode.N;
            case 18: return KeyCode.O;
            case 19: return KeyCode.P;
            case 20: return KeyCode.Q;
            case 21: return KeyCode.R;
            case 22: return KeyCode.S;
            case 23: return KeyCode.T;
            case 24: return KeyCode.U;
            case 25: return KeyCode.V;
            case 26: return KeyCode.W;
            case 27: return KeyCode.X;
            case 28: return KeyCode.Y;
            case 29: return KeyCode.Z;
            case 231:
            case 227: return KeyCode.SYM;
            case 224: return KeyCode.CONTROL_LEFT;
            case 228: return KeyCode.CONTROL_RIGHT;
            case 225: return KeyCode.SHIFT_LEFT;
            case 229: return KeyCode.SHIFT_RIGHT;
            case 57: return KeyCode.CAPS_LOCK;
            case 58: return KeyCode.F1;
            case 59: return KeyCode.F2;
            case 60: return KeyCode.F3;
            case 61: return KeyCode.F4;
            case 62: return KeyCode.F5;
            case 63: return KeyCode.F6;
            case 64: return KeyCode.F7;
            case 65: return KeyCode.F8;
            case 66: return KeyCode.F9;
            case 67: return KeyCode.F10;
            case 68: return KeyCode.F11;
            case 69: return KeyCode.F12;
            case 70: return KeyCode.PRINT_SCREEN;
            case 71: return KeyCode.SCROLL_LOCK;
            case 72: return KeyCode.PAUSE;
            case 73: return KeyCode.INSERT;
            case 76: return KeyCode.DEL;
            case 74: return KeyCode.HOME;
            case 77: return KeyCode.END;
            case 75: return KeyCode.PAGE_UP;
            case 78: return KeyCode.PAGE_DOWN;
            case 79: return KeyCode.RIGHT;
            case 80: return KeyCode.LEFT;
            case 81: return KeyCode.DOWN;
            case 82: return KeyCode.UP;
            case 101: return KeyCode.APPLICATION;
            case 102: return KeyCode.POWER;
            case 118: return KeyCode.MENU;
            case 127: return KeyCode.MUTE;
            case 156: return KeyCode.CLEAR;
            case 226: return KeyCode.ALT_LEFT;
            case 230: return KeyCode.ALT_RIGHT;
        }
        return KeyCode.UNKNOWN;
    }
}
