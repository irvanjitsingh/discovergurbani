package com.irvanjit.discovergurbani;

import java.util.HashMap;

public class GurmukhiCharMap {
    HashMap<Character, Character> charMap;
    public GurmukhiCharMap(int mappingVersion) {
        charMap = new HashMap<Character, Character>();
        if (mappingVersion == 1) {
            charMap.put('q', 'ੳ');
            charMap.put('w', 'ਅ');
            charMap.put('e', 'ੇ');
            charMap.put('r', 'ਰ');
            charMap.put('t', 'ਤ');
            charMap.put('y', 'ਯ');
            charMap.put('u', 'ੁ');
            charMap.put('i', 'ਿ');
            charMap.put('o', 'ੋ');
            charMap.put('p', 'ਪ');
            charMap.put('a', 'ਾ');
            charMap.put('s', 'ਸ');
            charMap.put('d', 'ਦ');
            charMap.put('f', '੍');
            charMap.put('g', 'ਗ');
            charMap.put('h', 'ਹ');
            charMap.put('j', 'ਜ');
            charMap.put('k', 'ਕ');
            charMap.put('l', 'ਲ');
            charMap.put('z', 'ਙ');
            charMap.put('x', 'ੜ');
            charMap.put('c', 'ਚ');
            charMap.put('v', 'ਵ');
            charMap.put('b', 'ਬ');
            charMap.put('n', 'ਨ');
            charMap.put('m', 'ਮ');
            charMap.put('Q', 'ੲ');
            charMap.put('W', 'ਆ');
            charMap.put('E', 'ੈ');
//        charMap.put('R', '');
            charMap.put('T', 'ਥ');
//        charMap.put('Y', '');
            charMap.put('U', 'ੂ');
            charMap.put('I', 'ੀ');
            charMap.put('O', 'ੌ');
            charMap.put('P', 'ਫ');
            charMap.put('A', 'ਾ');
            charMap.put('S', 'ਸ਼');
            charMap.put('D', 'ਧ');
            charMap.put('F', '਼');
            charMap.put('G', 'ਘ');
            charMap.put('H', 'ਃ');
            charMap.put('J', 'ਝ');
            charMap.put('K', 'ਖ');
            charMap.put('L', 'ਲ਼');
//        charMap.put('Z', '');
//        charMap.put('X', 'ੜ੍ਹ');
            charMap.put('C', 'ਛ');
            charMap.put('V', 'ੴ');
            charMap.put('B', 'ਭ');
            charMap.put('N', 'ਣ');
            charMap.put('M', 'ਂ');
        } else if (mappingVersion == 2) {
            charMap.put('q', 'ਤ');
            charMap.put('w', 'ਾ');
            charMap.put('e', 'ੲ');
            charMap.put('r', 'ਰ');
            charMap.put('t', 'ਟ');
            charMap.put('y', 'ਯ');
            charMap.put('u', 'ੁ');
            charMap.put('i', 'ਿ');
            charMap.put('o', 'ੋ');
            charMap.put('p', 'ਪ');
            charMap.put('a', 'ਅ');
            charMap.put('s', 'ਸ');
            charMap.put('d', 'ਦ');
            charMap.put('f', 'ਡ');
            charMap.put('g', 'ਗ');
            charMap.put('h', 'ਹ');
            charMap.put('j', 'ਜ');
            charMap.put('k', 'ਕ');
            charMap.put('l', 'ਲ');
            charMap.put('z', 'ਙ');
            charMap.put('x', 'ੜ');
            charMap.put('c', 'ਚ');
            charMap.put('v', 'ਵ');
            charMap.put('b', 'ਬ');
            charMap.put('n', 'ਨ');
            charMap.put('m', 'ਮ');
            charMap.put('Q', 'ਥ');
//            charMap.put('W', '');
            charMap.put('E', 'ਓ');
//        charMap.put('R', '');
            charMap.put('T', 'ਥ');
//        charMap.put('Y', '');
            charMap.put('U', 'ੂ');
            charMap.put('I', 'ੀ');
            charMap.put('O', 'ੌ');
            charMap.put('P', 'ਫ');
            charMap.put('A', 'ਾ');
            charMap.put('S', 'ਸ਼');
            charMap.put('D', 'ਧ');
            charMap.put('F', 'ਢ');
            charMap.put('G', 'ਘ');
            charMap.put('H', 'ਃ');
            charMap.put('J', 'ਝ');
            charMap.put('K', 'ਖ');
            charMap.put('L', 'ਲ਼');
            charMap.put('Z', 'ੇ');
            charMap.put('X', 'ੈ');
            charMap.put('C', 'ਛ');
            charMap.put('V', 'ਞ');
            charMap.put('B', 'ਭ');
            charMap.put('N', 'ਣ');
            charMap.put('M', 'ਂ');
        }
    }

    public HashMap<Character, Character> getMapping() {
        return charMap;
    }
}
