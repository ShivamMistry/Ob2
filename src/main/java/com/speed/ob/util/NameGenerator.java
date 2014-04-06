package com.speed.ob.util;

import java.util.Random;

/**
 * See LICENSE.txt for license info
 */
public class NameGenerator {

    private int order;
    private int nonce;

    public NameGenerator(int nonce) {
        this.nonce = nonce;
    }

    public NameGenerator() {
        this.nonce = new Random().nextInt(26) + 1;
    }

    private static String base26(int n) {
        StringBuilder builder = new StringBuilder();
        while (n > 0) {
            builder.append((char) ('a' + --n % 26));
            n /= 26;
        }
        return builder.reverse().toString();
    }

    public String next() {
        return base26(nonce++);
    }

}
