/*
 * MIT License
 * 
 * Copyright (c) 2020 Sebastian Gössl
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */



package com.github.sebig3000.markov;

import java.util.HashMap;
import java.util.Map;



/**
 * WeightMap test.
 * Counts the random output of a WeightMap, mapping letters to their position
 * in the alphabet: {a:1, b:2, c:3, ...}.
 * Then outputs the counts and also normalizes them to compare the counts to
 * the initial weights.
 * 
 * @author Sebastian Gössl
 */
public class WeightMapTest {
    
    public static void main(String[] args) {
        
        //Generate a weight map with all letters and their position in the
        //alphabet as weights: a:1, b:2, c:3, ...
        final WeightMap<Character> map = new WeightMap<>();
        for(char i='a'; i<='z'; i++) {
            map.put(i, i-'a'+1);
        }
        System.out.println("Weight map: " + map);
        
        
        //Get n random weighted values and count their occurrences
        final int n = 1000000;
        System.out.println("Generating " + n + " random values");
        //Because a WeightMap can consume values by incrementing the respective
        //weight, it can be used to count occurences
        final WeightMap<Character> counter = new WeightMap<>();
        for(int i=0; i<n; i++) {
            counter.accept(map.get());
        }
        
        
        //Counts should be proportional to weights
        System.out.println("Number of occurrences: " + counter);
        
        //Normalize counts so they can be more easaly compared to the weights
        final Map<Character, Double> normalizedCounter = new HashMap<>();
        for(Map.Entry<Character, Integer> entry : counter.entrySet()) {
            normalizedCounter.put(entry.getKey(),
                    (double)map.getTotalWeights()/n * entry.getValue());
        }
        System.out.println("Nornalized occurences: " + normalizedCounter);
    }
}
