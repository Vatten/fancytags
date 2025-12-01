package dev.vatten.baserad;

import java.util.HashMap;
import java.util.Map;

public class CharWidthMap {
    public static final Map<Character, Integer> CHAR_MAP = new HashMap<>();

    static {
        CHAR_MAP.put('0', 6);
        CHAR_MAP.put('1', 6);
        CHAR_MAP.put('2', 6);
        CHAR_MAP.put('3', 6);
        CHAR_MAP.put('4', 6);
        CHAR_MAP.put('5', 6);
        CHAR_MAP.put('6', 6);
        CHAR_MAP.put('7', 6);
        CHAR_MAP.put('8', 6);
        CHAR_MAP.put('9', 6);

        CHAR_MAP.put(' ', 4);
        CHAR_MAP.put('!', 2);
        CHAR_MAP.put('#', 6);
        CHAR_MAP.put('$', 6);
        CHAR_MAP.put('%', 6);
        CHAR_MAP.put('&', 6);
        CHAR_MAP.put('\'', 2);
        CHAR_MAP.put('(', 4);
        CHAR_MAP.put(')', 4);
        CHAR_MAP.put('*', 4);
        CHAR_MAP.put('+', 6);
        CHAR_MAP.put(',', 2);
        CHAR_MAP.put('-', 6);
        CHAR_MAP.put('.', 2);
        CHAR_MAP.put('/', 6);
        CHAR_MAP.put(':', 2);
        CHAR_MAP.put(';', 2);
        CHAR_MAP.put('<', 5);
        CHAR_MAP.put('=', 6);
        CHAR_MAP.put('>', 5);
        CHAR_MAP.put('?', 6);
        CHAR_MAP.put('@', 7);

        CHAR_MAP.put('A', 6);
        CHAR_MAP.put('B', 6);
        CHAR_MAP.put('C', 6);
        CHAR_MAP.put('D', 6);
        CHAR_MAP.put('E', 6);
        CHAR_MAP.put('F', 6);
        CHAR_MAP.put('G', 6);
        CHAR_MAP.put('H', 6);
        CHAR_MAP.put('I', 4);
        CHAR_MAP.put('J', 6);
        CHAR_MAP.put('K', 6);
        CHAR_MAP.put('L', 6);
        CHAR_MAP.put('M', 6);
        CHAR_MAP.put('N', 6);
        CHAR_MAP.put('O', 6);
        CHAR_MAP.put('P', 6);
        CHAR_MAP.put('Q', 6);
        CHAR_MAP.put('R', 6);
        CHAR_MAP.put('S', 6);
        CHAR_MAP.put('T', 6);
        CHAR_MAP.put('U', 6);
        CHAR_MAP.put('V', 6);
        CHAR_MAP.put('W', 6);
        CHAR_MAP.put('X', 6);
        CHAR_MAP.put('Y', 6);
        CHAR_MAP.put('Z', 6);

        CHAR_MAP.put('[', 4);
        CHAR_MAP.put(']', 4);
        CHAR_MAP.put('^', 6);
        CHAR_MAP.put('_', 6);

        CHAR_MAP.put('a', 6);
        CHAR_MAP.put('b', 6);
        CHAR_MAP.put('c', 6);
        CHAR_MAP.put('d', 6);
        CHAR_MAP.put('e', 6);
        CHAR_MAP.put('f', 5);
        CHAR_MAP.put('g', 6);
        CHAR_MAP.put('h', 6);
        CHAR_MAP.put('i', 2);
        CHAR_MAP.put('j', 6);
        CHAR_MAP.put('k', 5);
        CHAR_MAP.put('l', 3);
        CHAR_MAP.put('m', 6);
        CHAR_MAP.put('n', 6);
        CHAR_MAP.put('o', 6);
        CHAR_MAP.put('p', 6);
        CHAR_MAP.put('q', 6);
        CHAR_MAP.put('r', 6);
        CHAR_MAP.put('s', 6);
        CHAR_MAP.put('t', 4);
        CHAR_MAP.put('u', 6);
        CHAR_MAP.put('v', 6);
        CHAR_MAP.put('w', 6);
        CHAR_MAP.put('x', 6);
        CHAR_MAP.put('y', 6);
        CHAR_MAP.put('z', 6);

        CHAR_MAP.put('{', 4);
        CHAR_MAP.put('|', 2);
        CHAR_MAP.put('}', 4);
        CHAR_MAP.put('~', 7);
    }
}
