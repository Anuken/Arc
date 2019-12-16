package io.anuke.arc.backends.teavm;

import io.anuke.arc.input.*;
import org.teavm.jso.dom.events.*;

public class TeaVMKeymap{
    public static KeyCode getButton(int button){
        switch(button){
            case MouseEvent.LEFT_BUTTON:
                return KeyCode.MOUSE_LEFT;
            case MouseEvent.RIGHT_BUTTON:
                return KeyCode.MOUSE_RIGHT;
            case MouseEvent.MIDDLE_BUTTON:
                return KeyCode.MOUSE_MIDDLE;
            default:
                return KeyCode.MOUSE_LEFT;
        }
    }

    public static KeyCode getCode(int keyCode){
        switch(keyCode){
            case KEY_ALT:
                return KeyCode.ALT_LEFT;
            case KEY_BACKSPACE:
                return KeyCode.BACKSPACE;
            case KEY_CTRL:
                return KeyCode.CONTROL_LEFT;
            case KEY_DELETE:
                return KeyCode.DEL;
            case KEY_DOWN:
                return KeyCode.DOWN;
            case KEY_END:
                return KeyCode.END;
            case KEY_ENTER:
                return KeyCode.ENTER;
            case KEY_ESCAPE:
                return KeyCode.ESCAPE;
            case KEY_HOME:
                return KeyCode.HOME;
            case KEY_LEFT:
                return KeyCode.LEFT;
            case KEY_PAGEDOWN:
                return KeyCode.PAGE_DOWN;
            case KEY_PAGEUP:
                return KeyCode.PAGE_UP;
            case KEY_RIGHT:
                return KeyCode.RIGHT;
            case KEY_SHIFT:
                return KeyCode.SHIFT_LEFT;
            case KEY_TAB:
                return KeyCode.TAB;
            case KEY_UP:
                return KeyCode.UP;
            case KEY_PAUSE:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_CAPS_LOCK:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_SPACE:
                return KeyCode.SPACE;
            case KEY_INSERT:
                return KeyCode.INSERT;
            case KEY_0:
                return KeyCode.NUM_0;
            case KEY_1:
                return KeyCode.NUM_1;
            case KEY_2:
                return KeyCode.NUM_2;
            case KEY_3:
                return KeyCode.NUM_3;
            case KEY_4:
                return KeyCode.NUM_4;
            case KEY_5:
                return KeyCode.NUM_5;
            case KEY_6:
                return KeyCode.NUM_6;
            case KEY_7:
                return KeyCode.NUM_7;
            case KEY_8:
                return KeyCode.NUM_8;
            case KEY_9:
                return KeyCode.NUM_9;
            case KEY_A:
                return KeyCode.A;
            case KEY_B:
                return KeyCode.B;
            case KEY_C:
                return KeyCode.C;
            case KEY_D:
                return KeyCode.D;
            case KEY_E:
                return KeyCode.E;
            case KEY_F:
                return KeyCode.F;
            case KEY_G:
                return KeyCode.G;
            case KEY_H:
                return KeyCode.H;
            case KEY_I:
                return KeyCode.I;
            case KEY_J:
                return KeyCode.J;
            case KEY_K:
                return KeyCode.K;
            case KEY_L:
                return KeyCode.L;
            case KEY_M:
                return KeyCode.M;
            case KEY_N:
                return KeyCode.N;
            case KEY_O:
                return KeyCode.O;
            case KEY_P:
                return KeyCode.P;
            case KEY_Q:
                return KeyCode.Q;
            case KEY_R:
                return KeyCode.R;
            case KEY_S:
                return KeyCode.S;
            case KEY_T:
                return KeyCode.T;
            case KEY_U:
                return KeyCode.U;
            case KEY_V:
                return KeyCode.V;
            case KEY_W:
                return KeyCode.W;
            case KEY_X:
                return KeyCode.X;
            case KEY_Y:
                return KeyCode.Y;
            case KEY_Z:
                return KeyCode.Z;
            case KEY_LEFT_WINDOW_KEY:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_RIGHT_WINDOW_KEY:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_NUMPAD0:
                return KeyCode.NUMPAD_0;
            case KEY_NUMPAD1:
                return KeyCode.NUMPAD_1;
            case KEY_NUMPAD2:
                return KeyCode.NUMPAD_2;
            case KEY_NUMPAD3:
                return KeyCode.NUMPAD_3;
            case KEY_NUMPAD4:
                return KeyCode.NUMPAD_4;
            case KEY_NUMPAD5:
                return KeyCode.NUMPAD_5;
            case KEY_NUMPAD6:
                return KeyCode.NUMPAD_6;
            case KEY_NUMPAD7:
                return KeyCode.NUMPAD_7;
            case KEY_NUMPAD8:
                return KeyCode.NUMPAD_8;
            case KEY_NUMPAD9:
                return KeyCode.NUMPAD_9;
            case KEY_MULTIPLY:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_ADD:
                return KeyCode.PLUS;
            case KEY_SUBTRACT:
                return KeyCode.MINUS;
            case KEY_DECIMAL_POINT_KEY:
                return KeyCode.PERIOD;
            case KEY_DIVIDE:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_F1:
                return KeyCode.F1;
            case KEY_F2:
                return KeyCode.F2;
            case KEY_F3:
                return KeyCode.F3;
            case KEY_F4:
                return KeyCode.F4;
            case KEY_F5:
                return KeyCode.F5;
            case KEY_F6:
                return KeyCode.F6;
            case KEY_F7:
                return KeyCode.F7;
            case KEY_F8:
                return KeyCode.F8;
            case KEY_F9:
                return KeyCode.F9;
            case KEY_F10:
                return KeyCode.F10;
            case KEY_F11:
                return KeyCode.F11;
            case KEY_F12:
                return KeyCode.F12;
            case KEY_NUM_LOCK:
                return KeyCode.NUM;
            case KEY_SCROLL_LOCK:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_SEMICOLON:
                return KeyCode.SEMICOLON;
            case KEY_EQUALS:
                return KeyCode.EQUALS;
            case KEY_COMMA:
                return KeyCode.COMMA;
            case KEY_DASH:
                return KeyCode.MINUS;
            case KEY_PERIOD:
                return KeyCode.PERIOD;
            case KEY_FORWARD_SLASH:
                return KeyCode.SLASH;
            case KEY_GRAVE_ACCENT:
                return KeyCode.BACKTICK;
            case KEY_OPEN_BRACKET:
                return KeyCode.LEFT_BRACKET;
            case KEY_BACKSLASH:
                return KeyCode.BACKSLASH;
            case KEY_CLOSE_BRACKET:
                return KeyCode.RIGHT_BRACKET;
            case KEY_SINGLE_QUOTE:
                return KeyCode.APOSTROPHE;
            default:
                return KeyCode.UNKNOWN;
        }
    }

    // these are absent from KeyCodes; we know not why...
    private static final int KEY_PAUSE = 19;
    private static final int KEY_CAPS_LOCK = 20;
    private static final int KEY_SPACE = 32;
    private static final int KEY_INSERT = 45;
    private static final int KEY_0 = 48;
    private static final int KEY_1 = 49;
    private static final int KEY_2 = 50;
    private static final int KEY_3 = 51;
    private static final int KEY_4 = 52;
    private static final int KEY_5 = 53;
    private static final int KEY_6 = 54;
    private static final int KEY_7 = 55;
    private static final int KEY_8 = 56;
    private static final int KEY_9 = 57;
    private static final int KEY_A = 65;
    private static final int KEY_B = 66;
    private static final int KEY_C = 67;
    private static final int KEY_D = 68;
    private static final int KEY_E = 69;
    private static final int KEY_F = 70;
    private static final int KEY_G = 71;
    private static final int KEY_H = 72;
    private static final int KEY_I = 73;
    private static final int KEY_J = 74;
    private static final int KEY_K = 75;
    private static final int KEY_L = 76;
    private static final int KEY_M = 77;
    private static final int KEY_N = 78;
    private static final int KEY_O = 79;
    private static final int KEY_P = 80;
    private static final int KEY_Q = 81;
    private static final int KEY_R = 82;
    private static final int KEY_S = 83;
    private static final int KEY_T = 84;
    private static final int KEY_U = 85;
    private static final int KEY_V = 86;
    private static final int KEY_W = 87;
    private static final int KEY_X = 88;
    private static final int KEY_Y = 89;
    private static final int KEY_Z = 90;
    private static final int KEY_LEFT_WINDOW_KEY = 91;
    private static final int KEY_RIGHT_WINDOW_KEY = 92;
    private static final int KEY_SELECT_KEY = 93;
    private static final int KEY_NUMPAD0 = 96;
    private static final int KEY_NUMPAD1 = 97;
    private static final int KEY_NUMPAD2 = 98;
    private static final int KEY_NUMPAD3 = 99;
    private static final int KEY_NUMPAD4 = 100;
    private static final int KEY_NUMPAD5 = 101;
    private static final int KEY_NUMPAD6 = 102;
    private static final int KEY_NUMPAD7 = 103;
    private static final int KEY_NUMPAD8 = 104;
    private static final int KEY_NUMPAD9 = 105;
    private static final int KEY_MULTIPLY = 106;
    private static final int KEY_ADD = 107;
    private static final int KEY_SUBTRACT = 109;
    private static final int KEY_DECIMAL_POINT_KEY = 110;
    private static final int KEY_DIVIDE = 111;
    private static final int KEY_F1 = 112;
    private static final int KEY_F2 = 113;
    private static final int KEY_F3 = 114;
    private static final int KEY_F4 = 115;
    private static final int KEY_F5 = 116;
    private static final int KEY_F6 = 117;
    private static final int KEY_F7 = 118;
    private static final int KEY_F8 = 119;
    private static final int KEY_F9 = 120;
    private static final int KEY_F10 = 121;
    private static final int KEY_F11 = 122;
    private static final int KEY_F12 = 123;
    private static final int KEY_NUM_LOCK = 144;
    private static final int KEY_SCROLL_LOCK = 145;
    private static final int KEY_SEMICOLON = 186;
    private static final int KEY_EQUALS = 187;
    private static final int KEY_COMMA = 188;
    private static final int KEY_DASH = 189;
    private static final int KEY_PERIOD = 190;
    private static final int KEY_FORWARD_SLASH = 191;
    private static final int KEY_GRAVE_ACCENT = 192;
    private static final int KEY_OPEN_BRACKET = 219;
    private static final int KEY_BACKSLASH = 220;
    private static final int KEY_CLOSE_BRACKET = 221;
    private static final int KEY_SINGLE_QUOTE = 222;
    private static final int KEY_ALT = 18;
    private static final int KEY_BACKSPACE = 8;
    private static final int KEY_CTRL = 17;
    private static final int KEY_DELETE = 46;
    private static final int KEY_DOWN = 40;
    private static final int KEY_END = 35;
    private static final int KEY_ENTER = 13;
    private static final int KEY_ESCAPE = 27;
    private static final int KEY_HOME = 36;
    private static final int KEY_LEFT = 37;
    private static final int KEY_PAGEDOWN = 34;
    private static final int KEY_PAGEUP = 33;
    private static final int KEY_RIGHT = 39;
    private static final int KEY_SHIFT = 16;
    private static final int KEY_TAB = 9;
    private static final int KEY_UP = 38;
}
