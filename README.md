# Pairwiser
Library for generate test cases based by pairwise theory 

Base theory
https://en.wikipedia.org/wiki/All-pairs_testing

# Example
We have system with 10 parameters. As example REST API with input json.

## List of parameters and possible values:

* P0=[p0v0, p0v1, p0v2, p0v3, p0v4, p0v5, p0v6], 
* P1=[p1v0, p1v1], 
* P2=[p2v0, p2v1, p2v2, p2v3, p2v4, p2v5], 
* P3=[p3v0, p3v1], 
* P4=[p4v0, p4v1, p4v2, p4v3, p4v4, p4v5], 
* P5=[p5v0, p5v1, p5v2], 
* P6=[p6v0, p6v1], 
* P7=[p7v0, p7v1, p7v2], 
* P8=[p8v0, p8v1, p8v2, p8v3, p8v4, p8v5], 
* P9=[p9v0, p9v1]

## Micro explain

Bruteforce test cases count is 7 * 2 * 6 * 2 * 6 * 3 * 2 * 3 * 6 * 2 = 217728
Pairwise test cases count = 55

This test cases covered test system by rule "All pairs of params need to add at least once to tests"

## Final cases:

* [p0v0, p2v0, p4v0, p8v0, p5v0, p7v0, p1v0, p3v0, p6v0, p9v0]
* [p0v0, p2v1, p4v1, p8v1, p5v1, p7v1, p1v1, p3v1, p6v1, p9v1]
* [p0v0, p2v2, p4v2, p8v2, p5v2, p7v2, p1v0, p3v1, p6v0, p9v1]
* [p0v0, p2v3, p4v3, p8v3, p5v0, p7v1, p1v1, p3v0, p6v1, p9v0]
* [p0v0, p2v4, p4v4, p8v4, p5v1, p7v0, p1v1, p3v0, p6v0, p9v1]
* [p0v0, p2v5, p4v5, p8v5, p5v2, p7v2, p1v0, p3v1, p6v1, p9v0]
* [p0v1, p2v0, p4v1, p8v2, p5v0, p7v2, p1v1, p3v0, p6v1, p9v0]
* [p0v1, p2v1, p4v0, p8v3, p5v2, p7v0, p1v0, p3v1, p6v0, p9v1]
* [p0v1, p2v2, p4v3, p8v0, p5v1, p7v1, p1v0, p3v1, p6v0, p9v0]
* [p0v1, p2v3, p4v2, p8v1, p5v1, p7v0, p1v1, p3v0, p6v1, p9v1]
* [p0v1, p2v4, p4v5, p8v4, p5v0, p7v1, p1v0, p3v1, p6v1, p9v0]
* [p0v1, p2v5, p4v4, p8v5, p5v2, p7v2, p1v1, p3v0, p6v0, p9v1]
* [p0v2, p2v0, p4v2, p8v3, p5v1, p7v1, p1v0, p3v1, p6v0, p9v0]
* [p0v2, p2v1, p4v3, p8v2, p5v0, p7v0, p1v1, p3v0, p6v1, p9v1]
* [p0v2, p2v2, p4v0, p8v1, p5v2, p7v2, p1v1, p3v0, p6v1, p9v0]
* [p0v2, p2v3, p4v1, p8v0, p5v2, p7v2, p1v0, p3v1, p6v0, p9v1]
* [p0v2, p2v4, p4v4, p8v5, p5v0, p7v1, p1v0, p3v1, p6v1, p9v0]
* [p0v2, p2v5, p4v5, p8v4, p5v1, p7v0, p1v1, p3v0, p6v0, p9v1]
* [p0v3, p2v0, p4v3, p8v1, p5v2, p7v2, p1v0, p3v0, p6v0, p9v1]
* [p0v3, p2v1, p4v2, p8v0, p5v0, p7v0, p1v1, p3v1, p6v1, p9v0]
* [p0v3, p2v2, p4v1, p8v3, p5v1, p7v1, p1v0, p3v0, p6v0, p9v0]
* [p0v3, p2v3, p4v0, p8v2, p5v1, p7v1, p1v1, p3v1, p6v1, p9v1]
* [p0v3, p2v4, p4v4, p8v4, p5v2, p7v2, p1v0, p3v0, p6v0, p9v0]
* [p0v3, p2v5, p4v5, p8v5, p5v0, p7v0, p1v1, p3v1, p6v1, p9v1]
* [p0v4, p2v0, p4v4, p8v0, p5v0, p7v0, p1v0, p3v0, p6v0, p9v0]
* [p0v4, p2v1, p4v5, p8v4, p5v1, p7v2, p1v1, p3v1, p6v1, p9v1]
* [p0v4, p2v2, p4v0, p8v5, p5v2, p7v1, p1v0, p3v0, p6v0, p9v0]
* [p0v4, p2v3, p4v1, p8v1, p5v0, p7v0, p1v1, p3v1, p6v1, p9v1]
* [p0v4, p2v4, p4v2, p8v2, p5v1, p7v1, p1v0, p3v0, p6v0, p9v0]
* [p0v4, p2v5, p4v3, p8v3, p5v2, p7v2, p1v1, p3v1, p6v1, p9v1]
* [p0v5, p2v0, p4v5, p8v0, p5v0, p7v0, p1v0, p3v0, p6v0, p9v0]
* [p0v5, p2v1, p4v4, p8v1, p5v1, p7v1, p1v1, p3v1, p6v1, p9v1]
* [p0v5, p2v2, p4v0, p8v4, p5v2, p7v2, p1v0, p3v0, p6v0, p9v0]
* [p0v5, p2v3, p4v1, p8v5, p5v1, p7v0, p1v1, p3v1, p6v1, p9v1]
* [p0v5, p2v4, p4v3, p8v3, p5v0, p7v1, p1v0, p3v0, p6v0, p9v0]
* [p0v5, p2v5, p4v2, p8v2, p5v2, p7v2, p1v1, p3v1, p6v1, p9v1]
* [p0v6, p2v0, p4v0, p8v4, p5v0, p7v0, p1v0, p3v0, p6v0, p9v0]
* [p0v6, p2v1, p4v1, p8v5, p5v1, p7v1, p1v1, p3v1, p6v1, p9v1]
* [p0v6, p2v2, p4v4, p8v2, p5v2, p7v2, p1v0, p3v0, p6v0, p9v0]
* [p0v6, p2v3, p4v5, p8v1, p5v0, p7v0, p1v1, p3v1, p6v1, p9v1]
* [p0v6, p2v4, p4v2, p8v0, p5v1, p7v1, p1v0, p3v0, p6v0, p9v0]
* [p0v6, p2v5, p4v3, p8v3, p5v2, p7v2, p1v1, p3v1, p6v1, p9v1]
* [p0v0, p2v5, p4v0, p8v0, p5v0, p7v1, p1v0, p3v0, p6v0, p9v0]
* [p0v0, p2v5, p4v1, p8v4, p5v1, p7v0, p1v1, p3v1, p6v1, p9v1]
* [p0v0, p2v2, p4v5, p8v2, p5v2, p7v2, p1v0, p3v0, p6v0, p9v0]
* [p0v0, p2v4, p4v0, p8v1, p5v0, p7v0, p1v1, p3v1, p6v1, p9v1]
* [p0v0, p2v4, p4v1, p8v3, p5v1, p7v1, p1v0, p3v0, p6v0, p9v0]
* [p0v0, p2v3, p4v4, p8v5, p5v2, p7v2, p1v1, p3v1, p6v1, p9v1]
* [p0v0, p2v5, p4v0, p8v1, p5v0, p7v0, p1v0, p3v0, p6v0, p9v0]
* [p0v0, p2v0, p4v2, p8v5, p5v1, p7v1, p1v1, p3v1, p6v1, p9v1]
* [p0v0, p2v3, p4v2, p8v4, p5v2, p7v2, p1v0, p3v0, p6v0, p9v0]
* [p0v0, p2v2, p4v5, p8v3, p5v0, p7v0, p1v1, p3v1, p6v1, p9v1]
* [p0v0, p2v0, p4v4, p8v3, p5v1, p7v1, p1v0, p3v0, p6v0, p9v0]
* [p0v0, p2v0, p4v3, p8v4, p5v2, p7v2, p1v1, p3v1, p6v1, p9v1]
* [p0v0, p2v0, p4v3, p8v5, p5v0, p7v0, p1v0, p3v0, p6v0, p9v0]

# License

Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/
