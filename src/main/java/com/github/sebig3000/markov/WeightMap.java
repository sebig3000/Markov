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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;



/**
 * Weight map used to get weighted random values.
 * Behaves like a normal map out of which keys can be randomly be chosen based
 * on their respective value interpreted as weight.
 * 
 * All operations happen through common interfaces.
 * Key-value (key-weight) pairs are manipulated by the {@link Map} interface.
 * All of these methods get delegated to an underlying {@link HashMap}, to
 * whose documentation should be referred for more detailed information about
 * this class behaviour.
 * To get a random key, use the {@link Supplier} or {@link Iterator} interface
 * with <code>get</code> or <code>next</code>.
 * Using <code>accept</code> of the {@link Consumer} interface, the weight of
 * the given key can be incremented (used for training applications, like for
 * Markov chains).
 * 
 * @param <T> the type of keys maintained by this map
 * 
 * @author Sebastian Gössl
 * @version 1.0 09.07.2020
 */
public class WeightMap<T> implements Map<T, Integer>,
        Supplier<T>, Consumer<T>, Iterator<T> {
    
    /** Key-weight map. Delegation was chosen over inheritance so that all
     * ways, on which the weights (=values) can be changed (put, remove, etc.),
     * are known and overridden. Otherwise, if all HashMap methods with said
     * behaviour would have to be extended with the totalWeights handling, and
     * that would not be as clear as this solution. */
    private final Map<T, Integer> map = new HashMap<>();
    /** Sum of all weights. Needed for random selection */
    private int totalWeights = 0;
    /** Random number generator used for key selection. */
    private final Random rand;
    /** Last key returned by (Iterator.)next, used for (Iterator.)remove. */
    private T lastKey;
    
    
    
    /**
     * Constructs a new <code>WeightMap</code> with the given map.
     * The content of the given map is copied and not wrapped.
     * 
     * @param map map with the given keys and weights
     */
    public WeightMap(Map<? extends T, ? extends Integer> map) {
        this();
        putAll(map);
    }
    
    /**
     * Constructs a new empty <code>WeightMap</code>.
     */
    public WeightMap() {
        this(new Random());
    }
    
    /**
     * Constructs a new empty <code>WeightMap</code> with the given seed to
     * initialize the internal random number generator.
     * 
     * @param seed seed for the internal random number generator
     */
    public WeightMap(long seed) {
        this(new Random(seed));
    }
    
    /**
     * Constructs a new empty <code>WeightMap</code>.
     * 
     * @param rand uses the given random number generator as internal number
     * generator
     */
    public WeightMap(Random rand) {
        this.rand = rand;
    }
    
    
    
    /**
     * Returns the sum of all weights.
     * 
     * @return sum of all weights
     */
    public int getTotalWeights() {
        return totalWeights;
    }
    
    
    
    //Map
    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return map.size();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public Integer get(Object key) {
        return map.get(key);
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public Integer put(T key, Integer value) {
        if(value < 0) {
            throw new IllegalArgumentException("Weight less or equal to 0");
        }
        
        
        totalWeights += value;
        
        final Integer last = map.put(key, value);
        if(last != null) {
            totalWeights -= last;
        }
        
        return last;
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public Integer remove(Object key) {
        final Integer last = map.remove(key);
        
        if(last != null) {
            totalWeights -= last;
        }
        
        return last;
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public void putAll(Map<? extends T, ? extends Integer> m) {
        for(Entry<? extends T, ? extends Integer> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public void clear() {
        map.clear();
        totalWeights = 0;
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public Set<T> keySet() {
        return map.keySet();
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public Collection<Integer> values() {
        return map.values();
    }
    
    /**
     * {@inheritdoc}
     */
    @Override
    public Set<Entry<T, Integer>> entrySet() {
        return map.entrySet();
    }
    
    
    
    //Supplier
    /**
     * Returns a random key.
     * 
     * @return random key
     * @throws NoSuchElementException if map is empty
     */
    @Override
    public T get() {
        if(totalWeights <= 0) {
            //Avoid exception from rand.nextInt(totalWeights)
            //if totalWeight is 0 (means there are no keys in map)
            throw new NoSuchElementException("Map is empty");
        }
        
        
        //Choose random cumulative distribution function value
        // -> return corresponding x (sort of)
        int value = rand.nextInt(totalWeights);
        
        //Consistent order is not guaranteed, but should not be problem
        for(Entry<T, Integer> entry : entrySet()) {
            if(entry.getValue() > value) {
                return entry.getKey();
            }
            value -= entry.getValue();
        }
        
        throw new ArithmeticException("Weights are broken");
    }
    
    
    
    //Consumer
    /**
     * Increments the weight of the given key or adds it to this map with
     * a weight of 1, if it is not yet contained.
     * 
     * @param key key whose weight should be incremented
     */
    @Override
    public void accept(T key) {
        Integer currentWeight = map.get(key);
        
        if(currentWeight == null) {
            currentWeight = 0;
        }
        
        put(key, currentWeight+1);
    }
    
    
    
    //Iterator
    /**
     * Returns if this map has keys to randomly choose from.
     * Equivalent to <code>!isEmpty()</code>.
     * 
     * @return if this map has keys to randomly choose from
     */
    @Override
    public boolean hasNext() {
        return !isEmpty();
    }
    
    /**
     * Returns a random key.
     * Equivalent to <code>get</code>.
     * 
     * @return random key
     */
    @Override
    public T next() {
        lastKey = get();
        return lastKey;
    }
    
    /**
     * Removes the last key returned by <code>next</code>.
     * Keys returned by <code>get</code> are ignored.
     */
    @Override
    public void remove() {
        remove(lastKey);
    }
    
    
    
    //Object
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return map.hashCode();
    }
    
    /**
     * {@inheritDoc}
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
        
        final WeightMap<T> other = (WeightMap<T>)obj;
        return Objects.equals(this.map, other.map);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return map.toString();
    }
}
