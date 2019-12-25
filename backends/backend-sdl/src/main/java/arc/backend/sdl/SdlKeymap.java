package arc.backend.sdl;

import arc.input.*;

public class SdlKeymap{

    public static KeyCode getCode(int code){
        switch(code){
            case 0: return KeyCode.UNKNOWN;
            case 8: return KeyCode.BACKSPACE;
            case 9: return KeyCode.TAB;
            case 1073741982:
            case 13: return KeyCode.ENTER;
            case 27: return KeyCode.ESCAPE;
            case 32: return KeyCode.SPACE;
            case 43: return KeyCode.PLUS;
            case 44: return KeyCode.COMMA;
            case 45: return KeyCode.MINUS;
            case 46: return KeyCode.PERIOD;
            case 47: return KeyCode.SLASH;
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
            case 58: return KeyCode.COLON;
            case 59: return KeyCode.SEMICOLON;
            case 61: return KeyCode.EQUALS;
            case 64: return KeyCode.AT;
            case 92: return KeyCode.BACKSLASH;
            case 96: return KeyCode.BACKTICK;
            case 97: return KeyCode.A;
            case 98: return KeyCode.B;
            case 99: return KeyCode.C;
            case 100: return KeyCode.D;
            case 101: return KeyCode.E;
            case 102: return KeyCode.F;
            case 103: return KeyCode.G;
            case 104: return KeyCode.H;
            case 105: return KeyCode.I;
            case 106: return KeyCode.J;
            case 107: return KeyCode.K;
            case 108: return KeyCode.L;
            case 109: return KeyCode.M;
            case 110: return KeyCode.N;
            case 111: return KeyCode.O;
            case 112: return KeyCode.P;
            case 113: return KeyCode.Q;
            case 114: return KeyCode.R;
            case 115: return KeyCode.S;
            case 116: return KeyCode.T;
            case 117: return KeyCode.U;
            case 118: return KeyCode.V;
            case 119: return KeyCode.W;
            case 120: return KeyCode.X;
            case 121: return KeyCode.Y;
            case 122: return KeyCode.Z;
            case 127: return KeyCode.FORWARD_DEL;
            case 1073742048: return KeyCode.CONTROL_LEFT;
            case 1073742052: return KeyCode.CONTROL_RIGHT;
            case 1073742049: return KeyCode.SHIFT_LEFT;
            case 1073742053: return KeyCode.SHIFT_RIGHT;
            case 1073741882: return KeyCode.F1;
            case 1073741883: return KeyCode.F2;
            case 1073741884: return KeyCode.F3;
            case 1073741885: return KeyCode.F4;
            case 1073741886: return KeyCode.F5;
            case 1073741887: return KeyCode.F6;
            case 1073741888: return KeyCode.F7;
            case 1073741889: return KeyCode.F8;
            case 1073741890: return KeyCode.F9;
            case 1073741891: return KeyCode.F10;
            case 1073741892: return KeyCode.F11;
            case 1073741893: return KeyCode.F12;
            case 1073741897: return KeyCode.INSERT;
            case 1073741898: return KeyCode.HOME;
            case 1073741901: return KeyCode.END;
            case 1073741903: return KeyCode.RIGHT;
            case 1073741904: return KeyCode.LEFT;
            case 1073741905: return KeyCode.DOWN;
            case 1073741906: return KeyCode.UP;
            case 1073741926: return KeyCode.POWER;
            case 1073741942: return KeyCode.MENU;
            case 1073741951: return KeyCode.MUTE;
            case 1073741980: return KeyCode.CLEAR;
            case 1073742050: return KeyCode.ALT_LEFT;
            case 1073742054: return KeyCode.ALT_RIGHT;
        }
        return KeyCode.UNKNOWN;
    }
}
