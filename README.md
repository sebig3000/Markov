# Markov

![License](https://img.shields.io/github/license/sebig3000/Markov)

Markov chain Java implementation including standalone weighted random selection
functionality.
This library consists out of two classes:
 * WeightMap
 * MarkovNode

![UML](/UML.png)


## [WeightMap](/src/main/java/com/github/sebig3000/markov/WeightMap.java)

Weight map used to get (integer-)weighted random values.
Behaves like a normal map out of which keys can be randomly be chosen based on
their respective value interpreted as weight.

The key type can be specified via generics.
```
//Creates a new map that maps string objects to weights
WeightMap<String> map = new WeightMap<>();
```

All operations happen through common interfaces.
Key-value (key-weight) pairs are manipulated by the Map interface.
```
//Sets the weight for the key "Hello" to 4
map.put("Hello", 4);
```
```
//Find out what weight the key "Hello" maps to (4)
int weight = map.get("Hello");
```
All of these methods get delegated to an underlying HashMap, to whose
documentation should be referred for more detailed information about this
classes' behaviour.

To get a random key, use the Supplier or Iterator interface (next, hasNext &
remove).
```
//Get weighted random key
String selection = map.get();
```

Using accept of the Consumer interface, the weight of the given key can be
incremented (used for training applications, like for Markov chains).
```
//Increments the weight for "Hello" by 1 (to 5)
map.accept("Hello");
```

[Short example.](/src/test/java/com/github/sebig3000/markov/WeightMapTest.java)



## [MarkovNode](/src/main/java/com/github/sebig3000/markov/MarkovNode.java)

This class represents the states in a Markov chain as nodes in a graph.
This class extends [WeightMap](/src/main/java/com/github/sebig3000/markov/WeightMap.java),
which handles the weighted random child selection.

For training use the BiFunction interface.
It searches the existing nodes for the following data, creates a new node if
needed and adds it to the set, increments it's weight for it and returns it, so
training can happen in a single line.
```
//Markov chain that handles strings
final Set<MarkovNode<String>> nodes = new HashSet<>();
final MarkovNode<String> start = new MarkovNode<>();

MarkovNode<String> current = start;
for(String word : words) {
    current = current.apply(nodes, word);
}
```

To generate sequences of data from a Markov chain use
this classes' Iterable interface and not the WeightMap's Iterator interface.
The latter doesn't traverse over the nodes but just stays on the respective
node and keeps randomly returning it's children.
```
final Iterator<String> iterator = start.iterator();
while(iterator.hasNext()) {
    System.out.print(iterator.next() + ' ');
}
```

[Short example.](/src/test/java/com/github/sebig3000/markov/MarkovNodeTest.java)



## How does it work?

### [WeightMap](/src/main/java/com/github/sebig3000/markov/WeightMap.java)

To get a weighted random selection all keys will be flattened out, with their
weights defining their width. Then a random integer between (including) 0 and
(excluding) totalWeights (6) is used to pick the key, the random value "lands
on". Double the weight means doubled width and therefore double the chance to
get selected.
![numberline](/numberline.png)

### [MarkovNode](/src/main/java/com/github/sebig3000/markov/MarkovNode.java)

The MarkovNode class then just adds a field to store a generic data that
represents it's state, and an iterator to make it easy to use.
![graph](/graph.png)



## Chosen design rules

 * Prefer simplicity over performance
 * Use of common interfaces



## TODO

 - [ ] Better documentation
 - [ ] Correct UML diagram



## License (MIT)

MIT License

Copyright (c) 2020 Sebastian Gössl

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
