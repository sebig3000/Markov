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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



/**
 * MarkovNode test.
 * Trains a Markov Chain (a set of MarkovNodes) on the Tensorflow Shakespeare
 * data set. Every node holds a "word" (punctuations, etc. not filtered). The
 * Chain should then generate full paragraphs.
 * 
 * @author Sebastian Gössl
 */
public class MarkovNodeTest {
    
    public static void main(String[] args) {
        
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                new URL("https://storage.googleapis.com/"
                        + "download.tensorflow.org/data/shakespeare.txt")
                        .openStream()))) {
            
            
            //Create graph with nodes
            final Set<MarkovNode<String>> nodes = new HashSet<>();
            final MarkovNode<String> start = new MarkovNode<>("$");
            final MarkovNode<String> end = new MarkovNode<>("\n");
            nodes.add(start);
            nodes.add(end);
            
            
            //Iterate over all lines
            String line;
            boolean lastLineEmpty = false;
            MarkovNode<String> current = start;
            //Limit number of lines so that training doesn't take to long
	    for(int i=0; (line = reader.readLine()) != null && i<1000; i++) {
                
                //New paragraph: jump to start
                if(lastLineEmpty && line.endsWith(":")) {
                    current.accept(end);
                    current = start;
                    
                } else {
                    //Split line into "words" and let each one be a node
                    final String[] words = line.split(" ");
                    for(String word : words) {
                        //Actual training step
                        current = current.apply(nodes, word);
                    }
                }
                
                lastLineEmpty = line.isEmpty();
	    }
            
            
            
            //Generate 10 paragraphs
            for(int i=0; i<10; i++) {
                final Iterator<String> iterator = start.iterator();
                while(iterator.hasNext()) {
                    final String word = iterator.next();
                    System.out.print(word.equals("\n")?word:(word+' '));
                }
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
