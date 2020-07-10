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

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;



/**
 * MarkovNode class.
 * This class represents the states in a Markov chain as nodes in a graph.
 * This class extends {@link WeightMap}, which handles the weighted random
 * child selection.
 * For training use the {@link BiFunction} interface.
 * To generate sequences of data from a Markov chain use this classes
 * {@link Iterable} interface and not the <code>WeightMap</code>s
 * {@link Iterator} interface. The latter doesn't traverse over the nodes but
 * just stays on the respective node and keeps randomly returning it's
 * children.
 * 
 * @param <T> the type of data contained by this node
 * 
 * @author Sebastian Gössl
 * @version 1.0 09.07.2020
 */
public class MarkovNode<T> extends WeightMap<MarkovNode<T>>
        implements Iterable<T>,
        BiFunction<Set<MarkovNode<T>>, T, MarkovNode<T>> {
    
    /** Data of this node. */
    private final T data;
    
    
    
    /**
     * Constructs a new <code>MarkovNode</code> with the same data of the
     * given node and a copy of the mapping. Copy constructor.
     * 
     * @param other node to copy
     */
    public MarkovNode(MarkovNode<T> other) {
        this(other.data, other);
    }
    
    /**
     * Constructs a new <code>MarkovNode</code> with the given data and a
     * copy of the given mapping.
     * 
     * @param data data this node should contain
     * @param map mapping to children this node should copy
     */
    public MarkovNode(T data,
            Map<? extends MarkovNode<T>, ? extends Integer> map) {
        this(data);
        putAll(map);
    }
    
    /**
     * Constructs a new MarkovNode containing null as data.
     */
    public MarkovNode() {
        this((T)null);
    }
    
    /**
     * Constructs a new MarkovNode containing the given data.
     * 
     * @param data data to be contained by this node
     */
    public MarkovNode(T data) {
        this.data = data;
    }
    
    
    
    /**
     * Returns the data of this node.
     * 
     * @return data of this node
     */
    public T getData() {
        return data;
    }
    
    
    
    /**
     * An iterator that iterates over the graph spanned by Markov nodes and
     * returns the data of the node it is currently on.
     * 
     * @param <E> the type of data contained by the nodes
     */
    public static class MarkovIterator<E> implements Iterator<E> {
        
        /**
         * Current node whose data hast been returned by the last next call.
         */
        private MarkovNode<E> node;
        
        
        
        /**
         * Constructs a new <code>MarkovIterator</code> which starts at the
         * given node.
         * The first returned data will be from a child of the given node.
         * 
         * @param node node to start from
         */
        public MarkovIterator(MarkovNode<E> node) {
            this(node, false);
        }
        
        /**
         * Constructs a new <code>MarkovIterator</code> which starts at the
         * given node.
         * If the first returned data will be from the given node can be
         * determined with the given boolean value.
         * 
         * @param node node to start from
         * @param returnFirst if the first returned data should be from the
         * given node
         */
        public MarkovIterator(MarkovNode<E> node, boolean returnFirst) {
            if(!returnFirst) {
                this.node = node;
            } else {
                this.node = new MarkovNode<>();
                this.node.accept(node);
            }
        }
        
        
        
        /**
         * {@inheritdoc}
         */
        @Override
        public boolean hasNext() {
            return node.hasNext();
        }
        
        /**
         * {@inheritdoc}
         */
        @Override
        public E next() {
            node = node.next();
            return node.getData();
        }
    }
    
    /**
     * Returns an iterator that iterates over the graph spanned by Markov
     * nodes and returns the data of the node it is currently on.
     * 
     * @return iterator that iterates through a Markov chain
     */
    @Override
    public Iterator<T> iterator() {
        return new MarkovIterator<>(this);
    }
    
    
    
    /**
     * Increments the weight of the child node with the given data and returns
     * the child node.
     * If a node with the given data doesn't exist yet, it is created
     * and added to the set.
     * If this node doesn't map to the child node, it is added to this
     * node with weight 1.
     * 
     * @param nodes set with nodes, where a new one is added if needed
     * @param childData data of the child whose weight should be incremented
     * @return child node
     */
    @Override
    public MarkovNode<T> apply(Set<MarkovNode<T>> nodes, T childData) {
        
        //Find child node
        MarkovNode<T> child = null;
        for(MarkovNode<T> node : nodes) {
            if(node.getData() == null ?
                    childData == null : node.getData().equals(childData)) {
                child = node;
                break;
            }
        }
        
        //If it doesn't yet exist, create a new one
        if(child == null) {
            child = new MarkovNode<>(childData);
            nodes.add(child);
        }
        
        //Increment weight
        accept(child);
        
        
        return child;
    }
    
    
    
    /**
     * Returns the hash code of the data contained by this node.
     * 
     * @return hash code of the data contained by this node
     */
    @Override
    public int hashCode() {
        return data.hashCode();
    }
    
    /**
     * Returns if this node's data equals the data of the given node.
     * 
     * @return if this node's data equals the data of the given node
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        
        //Just compare states and not the maps. Otherwise an infinite recursion
        //would happen
        final MarkovNode<?> other = (MarkovNode<?>) obj;
        return Objects.equals(this.data, other.data);
    }
}
