# Sha256
This project, written in Java, shows how the Sha256 Hashing algorithm can be considered to be a directed graph from the inputs to the outputs. 

Each bit is considered to be a node which is assigned once using a NOT, an AND, or an XOR. 

The graph is deterministic and the structure does not change for different inputs. Though the value that each node is assigned to does depend on the inputs. 

The input nodes are set at the start, all other nodes have either one precedent (when a NOT operator is used) or two precedents (when an XOR or AND is used). 

This algorithm does not try to be fast! If you want a quick implementation of Sha256 written in Java then don't use this code.

In my blog ([at this link](https://abitofmaths.blogspot.com/2022/06/she256-hashing-algorithm-as-directed.html)) I discuss what the code in this repository is trying to achieve.
