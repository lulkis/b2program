MACHINE StringSubset

VARIABLES  x

INVARIANT  x : POW(STRING)

INITIALISATION x := {"A", "B", "C"}

OPERATIONS

	op1 = SELECT x <: STRING THEN x := {"A"} END

END