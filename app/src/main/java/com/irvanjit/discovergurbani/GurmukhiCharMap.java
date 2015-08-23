package com.irvanjit.discovergurbani;

import java.util.HashMap;

class GurmukhiCharMap {
    private HashMap<Character, Character> charMap;
    public GurmukhiCharMap() {
        charMap = new HashMap<Character, Character>();

//        //osx gurmukhi querty mapping
//        charMap.put('q', 'ੳ');
//        charMap.put('w', 'ਅ');
//        charMap.put('e', 'ੇ');
//        charMap.put('r', 'ਰ');
//        charMap.put('t', 'ਤ');
//        charMap.put('y', 'ਯ');
//        charMap.put('u', 'ੁ');
//        charMap.put('i', 'ਿ');
//        charMap.put('o', 'ੋ');
//        charMap.put('p', 'ਪ');
//        charMap.put('a', 'ਾ');
//        charMap.put('s', 'ਸ');
//        charMap.put('d', 'ਦ');
//        charMap.put('f', '੍');
//        charMap.put('g', 'ਗ');
//        charMap.put('h', 'ਹ');
//        charMap.put('j', 'ਜ');
//        charMap.put('k', 'ਕ');
//        charMap.put('l', 'ਲ');
//        charMap.put('z', 'ਙ');
//        charMap.put('x', 'ੜ');
//        charMap.put('c', 'ਚ');
//        charMap.put('v', 'ਵ');
//        charMap.put('b', 'ਬ');
//        charMap.put('n', 'ਨ');
//        charMap.put('m', 'ਮ');
//        charMap.put('Q', 'ੲ');
//        charMap.put('W', 'ਆ');
//        charMap.put('E', 'ੈ');
////        charMap.put('R', '');
//        charMap.put('T', 'ਥ');
////        charMap.put('Y', '');
//        charMap.put('U', 'ੂ');
//        charMap.put('I', 'ੀ');
//        charMap.put('O', 'ੌ');
//        charMap.put('P', 'ਫ');
//        charMap.put('A', 'ਾ');
//        charMap.put('S', 'ਸ਼');
//        charMap.put('D', 'ਧ');
//        charMap.put('F', '਼');
//        charMap.put('G', 'ਘ');
//        charMap.put('H', 'ਃ');
//        charMap.put('J', 'ਝ');
//        charMap.put('K', 'ਖ');
//        charMap.put('L', 'ਲ਼');
////        charMap.put('Z', '');
////        charMap.put('X', 'ੜ੍ਹ');
//        charMap.put('C', 'ਛ');
//        charMap.put('V', 'ੴ');
//        charMap.put('B', 'ਭ');
//        charMap.put('N', 'ਣ');
//        charMap.put('M', 'ਂ');

        //custom mapping
        charMap.put('q', '\u0A24');
        charMap.put('w', '\u0A3E');
        charMap.put('e', '\u0A72');
        charMap.put('r', '\u0A24');
        charMap.put('t', '\u0A1F');
        charMap.put('y', '\u0A47');
        charMap.put('u', '\u0A41');
        charMap.put('i', '\u0A3F');
        charMap.put('o', '\u0A4B');
        charMap.put('p', '\u0A2A');
        charMap.put('a', '\u0A73');
        charMap.put('s', '\u0A38');
        charMap.put('d', '\u0A26');
        charMap.put('f', '\u0A21');
        charMap.put('g', '\u0A17');
        charMap.put('h', '\u0A39');
        charMap.put('j', '\u0A1C');
        charMap.put('k', '\u0A15');
        charMap.put('l', '\u0A32');
        charMap.put('z', '\u0A5B');
        charMap.put('x', '\u0A23');
        charMap.put('c', '\u0A1A');
        charMap.put('v', '\u0A35');
        charMap.put('b', '\u0A2C');
        charMap.put('n', '\u0A28');
        charMap.put('m', '\u0A2E');
        charMap.put('Q', '\u0A25');
//        charMap.put('W', '\u0A24');
        charMap.put('E', '\u0A13');
        // charMap.put('R', '\u0A24');
        charMap.put('T', '\u0A20');
        charMap.put('Y', '\u0A48');
        charMap.put('U', '\u0A42');
        charMap.put('I', '\u0A40');
        charMap.put('O', '\u0A4C');
        charMap.put('P', '\u0A2B');
        charMap.put('A', '\u0A05');
        charMap.put('S', '\u0A36');
        charMap.put('D', '\u0A27');
        charMap.put('F', '\u0A22');
        charMap.put('G', '\u0A18');
          // charMap.put('H', '\u0A24');
        charMap.put('J', '\u0A1D');
        charMap.put('K', '\u0A16');
        charMap.put('L', '\u0A33');
        charMap.put('Z', '\u0A5A');
        charMap.put('X', '\u0A2F');
        charMap.put('C', '\u0A1B');
        charMap.put('V', '\u0A5C');
        charMap.put('B', '\u0A2D');
        charMap.put('N', '\u0A70');
        charMap.put('M', '\u0A02');
    }

    public HashMap<Character, Character> getMapping() {
        return charMap;
    }
}
