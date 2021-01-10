# Pairwiser
Library for generate test cases based by pairwise theory

Base theory:
*[All-pairs_testing](https://en.wikipedia.org/wiki/All-pairs_testing)*
Base algorithm:
*[barbie.uta.edu](http://barbie.uta.edu/~fduan/ACTS/In-Parameter-Order_%20A%20Test%20Generation%20Strategy%20for%20Pairwise%20Testing.pdf)*

With some modifications

# Example
We have system with 10 parameters. As example REST API with input json.

## Code example
```java
Map<String, List<Object>> params = new HashMap<>();

for (int i = 0; i < 10; i++) {
	for (int j = 0; j < 10 * Math.random(); j++) {
		params.computeIfAbsent("P" + i, k -> new ArrayList<>()).add("p"+i+"v"+j);
	}
}

PairwiseGenerator<String, Object> gen = new PairwiseGenerator<>(params);
gen.stream().forEach(test -> {
	log.info("Test: {}", test);
});
```
Type Object used only as example, types of parameters can be any

## Maven dependency

```xml
<dependency>
  <groupId>com.anyqn.lib</groupId>
  <artifactId>pairwiser</artifactId>
  <version>0.1.9-SNAPSHOT</version>
</dependency>
```

## List of parameters and possible values

 * P0=[p0v0, p0v1, p0v2],
 * P1=[p1v0, p1v1, p1v2, p1v3],
 * P2=[p2v0, p2v1, p2v2, p2v3, p2v4, p2v5],
 * P3=[p3v0, p3v1, p3v2],
 * P4=[p4v0],
 * P5=[p5v0, p5v1, p5v2, p5v3, p5v4, p5v5],
 * P6=[p6v0, p6v1, p6v2, p6v3, p6v4],
 * P7=[p7v0, p7v1],
 * P8=[p8v0, p8v1, p8v2, p8v3, p8v4],
 * P9=[p9v0, p9v1, p9v2]

## Micro explain

Bruteforce test cases count is 3 * 4 * 6 * 3 * 1 * 6 * 5 * 2 * 5 * 3 = 194400
Pairwise test cases count = 41

This test cases covered test system by rule "All pairs of params need to add at least once to tests"

## Final cases:

* [p0v0, p1v0, p2v0, p3v0, p4v0, p5v0, p6v0, p7v0, p8v0, p9v0]
* [p0v1, p1v1, p2v0, p3v1, p4v0, p5v1, p6v1, p7v1, p8v1, p9v1]
* [p0v2, p1v2, p2v0, p3v2, p4v0, p5v2, p6v2, p7v0, p8v2, p9v2]
* [p0v0, p1v3, p2v0, p3v1, p4v0, p5v3, p6v3, p7v1, p8v3, p9v2]
* [p0v1, p1v0, p2v0, p3v2, p4v0, p5v4, p6v4, p7v1, p8v4, p9v1]
* [p0v2, p1v2, p2v0, p3v0, p4v0, p5v5, p6v0, p7v0, p8v1, p9v0]
* [p0v1, p1v3, p2v1, p3v0, p4v0, p5v0, p6v1, p7v0, p8v2, p9v1]
* [p0v0, p1v1, p2v1, p3v2, p4v0, p5v1, p6v2, p7v1, p8v0, p9v0]
* [p0v2, p1v0, p2v1, p3v1, p4v0, p5v2, p6v3, p7v0, p8v4, p9v2]
* [p0v2, p1v2, p2v1, p3v0, p4v0, p5v3, p6v4, p7v1, p8v3, p9v1]
* [p0v0, p1v3, p2v1, p3v2, p4v0, p5v4, p6v0, p7v0, p8v1, p9v2]
* [p0v1, p1v1, p2v1, p3v1, p4v0, p5v5, p6v1, p7v1, p8v0, p9v0]
* [p0v2, p1v1, p2v2, p3v1, p4v0, p5v0, p6v2, p7v0, p8v3, p9v2]
* [p0v1, p1v0, p2v2, p3v0, p4v0, p5v1, p6v3, p7v1, p8v2, p9v0]
* [p0v0, p1v3, p2v2, p3v2, p4v0, p5v2, p6v4, p7v1, p8v4, p9v1]
* [p0v1, p1v2, p2v2, p3v1, p4v0, p5v3, p6v0, p7v0, p8v0, p9v1]
* [p0v2, p1v2, p2v2, p3v2, p4v0, p5v4, p6v1, p7v0, p8v3, p9v0]
* [p0v0, p1v0, p2v2, p3v0, p4v0, p5v5, p6v2, p7v1, p8v1, p9v2]
* [p0v0, p1v1, p2v3, p3v2, p4v0, p5v0, p6v3, p7v1, p8v4, p9v1]
* [p0v2, p1v3, p2v3, p3v1, p4v0, p5v1, p6v4, p7v0, p8v2, p9v2]
* [p0v1, p1v0, p2v3, p3v0, p4v0, p5v2, p6v0, p7v1, p8v3, p9v0]
* [p0v0, p1v2, p2v3, p3v2, p4v0, p5v3, p6v1, p7v0, p8v1, p9v2]
* [p0v2, p1v3, p2v3, p3v0, p4v0, p5v4, p6v2, p7v0, p8v0, p9v1]
* [p0v1, p1v1, p2v3, p3v1, p4v0, p5v5, p6v3, p7v1, p8v2, p9v0]
* [p0v0, p1v2, p2v4, p3v0, p4v0, p5v0, p6v4, p7v0, p8v4, p9v0]
* [p0v1, p1v1, p2v4, p3v1, p4v0, p5v1, p6v0, p7v1, p8v3, p9v2]
* [p0v2, p1v0, p2v4, p3v2, p4v0, p5v2, p6v1, p7v0, p8v0, p9v1]
* [p0v1, p1v3, p2v4, p3v0, p4v0, p5v3, p6v2, p7v1, p8v4, p9v0]
* [p0v0, p1v1, p2v4, p3v1, p4v0, p5v4, p6v3, p7v0, p8v1, p9v1]
* [p0v2, p1v3, p2v4, p3v2, p4v0, p5v5, p6v4, p7v1, p8v2, p9v2]
* [p0v0, p1v0, p2v5, p3v0, p4v0, p5v0, p6v0, p7v0, p8v1, p9v0]
* [p0v1, p1v2, p2v5, p3v1, p4v0, p5v1, p6v1, p7v1, p8v4, p9v1]
* [p0v2, p1v1, p2v5, p3v2, p4v0, p5v2, p6v2, p7v0, p8v0, p9v2]
* [p0v0, p1v0, p2v5, p3v0, p4v0, p5v3, p6v3, p7v1, p8v2, p9v0]
* [p0v1, p1v3, p2v5, p3v1, p4v0, p5v4, p6v4, p7v0, p8v3, p9v1]
* [p0v2, p1v2, p2v5, p3v2, p4v0, p5v5, p6v0, p7v1, p8v4, p9v2]
* [p0v0, p1v1, p2v0, p3v0, p4v0, p5v3, p6v4, p7v0, p8v0, p9v0]
* [p0v1, p1v0, p2v0, p3v1, p4v0, p5v2, p6v4, p7v1, p8v1, p9v1]
* [p0v2, p1v2, p2v0, p3v2, p4v0, p5v0, p6v3, p7v0, p8v0, p9v2]
* [p0v0, p1v3, p2v0, p3v0, p4v0, p5v4, p6v0, p7v1, p8v2, p9v0]
* [p0v1, p1v0, p2v0, p3v1, p4v0, p5v5, p6v0, p7v0, p8v3, p9v1]


# Release instruction

mvn release:clean release:prepare
mvn release:perform

Important rule:

in pom.xml need to add gpg, javadoc and source maven plugins to release profile, because without this step release plugin do not execute this plugins.

# License

Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/
