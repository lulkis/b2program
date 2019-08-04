# B2Program

This is the code generator **B2Program** for generating code from B to other
programming languages.

A subset of B is supported for Java and C++ now. The work for code generation for Python, Clojure and C has begun but not continued.

Note:

* The implementation of the B types in C++ uses persistent set from:
  https://github.com/arximboldi/immer
* The library must first be installed before the generated C++ code can be used.
* The generated code for C works for a subset of the generated code that works
  for Java and C++.
* Sets and couples are not supported for C.
  Including other machines is not supported in C, too.
  The only types that are implemented for C are BInteger and BBoolean.
  An example where code generation for C works is the machine Lift.

## Supported subset of B

### Machine sections:

| Machine Section    | Usage                                                                                                  |
|--------------------|--------------------------------------------------------------------------------------------------------|
| SETS               |                                            S (Deferred Set)                                            |
|                    |                                   T = {e1, e2, ...} (Enumerated Set)                                   |
| CONSTANTS          |                                                x,y, ...                                                |
| CONCRETE_CONSTANTS |                                               cx, cy, ...                                              |
| PROPERTIES         | c = v (where c is a constant and v is a value)                                                         |
|                    | card(S) = n (where S is a deferred set and n is a number)                                              |
|                    | S = {c1,...,cn} & card(S) = n  (where S is a deferred set, c1,..., cn are constants and n is a number) |
| VARIABLES          | x,y, ...                                                                                               |
| CONCRETE_VARIABLES | cx, cy, ...                                                                                            |
| INVARIANT          | P (Logical Predicate)                                                                                  |
| ASSERTIONS         | P1;...;P2 (List Of Logical Predicates)                                                                 |
| INITIALISATION     |                                                                                                        |
| OPERATIONS         |                                                                                                        |


Note that code is not generated from INVARIANT and ASSERTIONS. These constructs are used for verifying the machine only.
CONSTRAINTS and DEFINITIONS clause are not supported for code generation.


### Machine inclusion:

| Machine inclusion | Usage                        |
|-------------------|------------------------------|
| INCLUDES          | M1 ... MN (List of Machines) |
| EXTENDS           | M1 ... MN (List of Machines) |

Other machine inclusion clauses (SEES, USES, PROMOTES and REFINES) are not supported yet.


### Logical Predicates:

| Predicate             | Meaning                    |
|-----------------------|----------------------------|
| P & Q                 | conjunction                |
| P or Q                | disjunction                |
| P => Q                | implication                |
| P <=> Q               | equivalence                |
| not P                 | negation                   |
| !(x1,...,xn).(P => Q) | universal quantification   |
| #(x1,...,xn).(P & Q)  | existential quantification |

Restriction: As universal quantifications and existential quantifications are quantified constructs, the predicate P must constraint the value of the variables x1, ..., xn.
P is a conjunction of n conjuncts where the i-th conjunct must constraint xi for each i in {1,...,n}.

### Equality:

| Predicate             | Meaning    |
|-----------------------|------------|
| E = F                 | equality   |
| E \= F                | inequality |

### Booleans:

| Boolean | Meaning                             |
|---------|-------------------------------------|
| TRUE    | true value                          |
| FALSE   | false value                         |
| BOOL    | set of boolean values {TRUE, FALSE} |
| bool(P) | convert predicate into BOOL value   |


### Sets:

| Set expression or predicate  | Meaining                                   |
|------------------------------|--------------------------------------------|
| {}                           | Empty Set                                  |
| {E}                          | Singleton Set                              |
| {E,F,...}                    | Set Enumeration                            |
| {x1,...,xn|P}                | Set Comprehension                          |
| POW(S)                       | Power Set                                  |
| POW1(S)                      | Set of Non-Empty Subsets                   |
| FIN(S)                       | Set of All Finite Subsets                  |
| FIN1(S)                      | Set of All Non-Empty Finite Subsets        |
| card(S)                      | Cardinality                                |
| S * T                        | Cartesian Product                          |
| S \/ T                       | Set Union                                  |
| S /\ T                       | Set Intersection                           |
| S - T                        | Set Difference                             |
| E : S                        | Element of                                 |
| E /: S                       | Not Element of                             |
| S <: T                       | Subset of                                  |
| S /<: T                      | Not Subset of                              |
| S <<: T                      | Strict Subset of                           |
| S /<<: T                     | Not Strict Subset of                       |
| union(S)                     | Generalized Union over Sets of Sets        |
| inter(S)                     | Generalized Intersection over Sets of Sets |
| UNION(z1,...,zn).(P|E)       | Generalized Union with Predicate           |
| INTER(z1,...,zn).(P|E)       | Generalized Intersection with Predicate    |

Restriction: Set comprehesions, generalized unions and generalized intersections are quantified constructs. The predicate P must be a conjunction where the first n conjuncts must constraint the bounded variables.
The i-th conjunct must constraint xi for each i in {1,...,n}.


### Numbers:

| Number expression or predicate | Meaning                                       |
|--------------------------------|-----------------------------------------------|
| INTEGER                        | Set of Integers                               |
| NATURAL                        | Set of Natural Numbers                        |
| NATURAL1                       | Set of Non-Zero Natural Numbers               |
| INT                            | Set of Implementable Integers                 |
| NAT                            | Set of Implementable Natural Numbers          |
| NAT1                           | Set of Non-Zero Implementable Natural Numbers |
| n..m                           | Set of Numbers from n to m                    |
| MININT                         | Minimum Implementable Integer                 |
| MAXINT                         | Maximum Implementable Integer                 |
| m > n                          | Greater Than                                  |
| m < n                          | Less Than                                     |
| m >= n                         | Greater Than or Equal                         |
| m <= n                         | Less Than Or Equal                            |
| max(S)                         | Maximum of a Set of Numbers                   |
| min(S)                         | Minimum of a Set of Numbers                   |
| m + n                          | Addition                                      |
| m - n                          | Difference                                    |
| m * n                          | Multiplication                                |
| m / n                          | Division                                      |
| m ** n                         | Power                                         |
| m mod n                        | Remainder of Division                         |
| PI(z1,...,zn).(P|E)            | Set product                                   |
| SIGMA(z1,...,zn).(P|E)         | Set summation                                 |
| succ(n)                        | Successor                                     |
| pred(n)                        | Predecessor                                   |

Restrictions:

INTEGER, NATURAL and NATURAL1 are infinite sets. They are only supported on the right-hand side of a set predicate.

Set product and set summation are quantified constructs. The predicate P must be a conjunction where the first n conjuncts must constraint the bounded variables.
The i-th conjunct must constraint xi for each i in {1,...,n}.

### Relations:

| Relation expression | Meaining                                              |
|---------------------|-------------------------------------------------------|
| S <-> T             | Set of relation                                       |
| E |-> F             | Couple                                                |
| dom(r)              | Domain of Relation                                    |
| range(r)            | Range of Relation                                     |
| id(S)               | Identity Relation                                     |
| S <| r              | Domain Restriction                                    |
| S <<| r             | Domain Substraction                                   |
| r |> S              | Range Restriction                                     |
| r |>> S             | Range Substraction                                    |
| r~                  | Inverse of Relation                                   |
| r[S]                | Relational Image                                      |
| r1 <+ r2            | Relational Overriding                                 |
| r1 >< r2            | Direct Product                                        |
| (r1 ; r2)           | Relational Composition                                |
| (r1 || r2)          | Parallel Product                                      |
| prj1(S,T)           | Projection Function                                   |
| prj2(S,T)           | Projection Function                                   |
| closure1(r)         | Transitive Closure                                    |
| closure(r)          | Transitive Reflxibe Closure                           |
| iterate(r,n)        | Iteration of r with n                                 |
| fnc(r)              | Translate Relation A <-> B into function A +-> POW(B) |
| rel(r)              | Translate Relation A <-> POW(B) into relation A <-> B |

Restriction: Set of Relation mostly grows up very fast. They are only supported on the right-hand side of a set predicate.




## Usage

### Starting the code generator

```bash
# Java
./gradlew run -Planguage="java" -Pbig_integer="true/false" [-Pminint="minint" -Pmaxint="maxint"] -Pdeferred_set_size="size" -Pfile="<path_relative_to_project_directory>"

# Python
./gradlew run -Planguage="python" -Pbig_integer="true/false" [-Pminint="minint" -Pmaxint="maxint"] -Pdeferred_set_size="size" -Pfile="<path_relative_to_project_directory>"

# C
./gradlew run -Planguage="c" -Pbig_integer="true/false" [-Pminint="minint" -Pmaxint="maxint"] -Pdeferred_set_size="size" -Pfile="<path_relative_to_project_directory>"

# C++
./gradlew run -Planguage="cpp" -Pbig_integer="true/false" [-Pminint="minint" -Pmaxint="maxint"] -Pdeferred_set_size="size" -Pfile="<path_relative_to_project_directory>"
```

-Pminint and -Pmaxint are optional. The default values cover 32-Bit Integers

### Compile the generated code in Java

1. Run `./gradlew build` in project btypes_persistent or btypes_big_integer
2. Move `btypes-all-2.9.12-SNAPSHOT.jar` to same folder as the generated classes
3. `javac -cp btypes-all-2.9.12-SNAPSHOT.jar <files....>`
4. Example: `javac -cp btypes-all-2.9.12-SNAPSHOT.jar TrafficLightExec.java TrafficLight.java`
  (Code generated from TrafficLightExec.mch which includes TrafficLight.mch)
  
### Compile the generated code in C++
  
1. Move all B types (see btypes_primitives or btypes_big_integer folder) files to same folder as the generated classes
2. `g++ -std=c++14 -O2 -march=native -g -DIMMER_NO_THREAD_SAFETY -o <executable> <main class>`
3. Example: `g++ -std=c++14 -O2 -march=native -g -DIMMER_NO_THREAD_SAFETY -o TrafficLightExec.exec TrafficLightExec.cpp`
   (TrafficLightExec.mch includes TrafficLight.mch, this command automatically compiles TrafficLight.cpp)

### Compile the generated code in C

1. Move BInteger and BBoolean to same folder as generated code (see btypes/src/main/c)
2. `gcc <input file> -o <output file>`
3. Example: `gcc Lift.c -o Lift`

### Execute the generated code in Java

1. Write a main function in the generated main class
2. `java -cp :btypes-all-2.9.12-SNAPSHOT.jar <main file>`
3. Example: `java -cp :btypes-all-2.9.12-SNAPSHOT.jar TrafficLightExec`

### Execute the generated code in C++

1. Write a main function in the generated main class
2. `./<main file>`
3. Example: `./TrafficLightExec.exec`

### Execute the generated code in C

1. Write a main function in the generated main file
2. `./main.c`
3. Example: `./Lift`

## Performance

Analysis of the Performance is described in
[benchmarks/README.md](microbenchmarks/README.md).
