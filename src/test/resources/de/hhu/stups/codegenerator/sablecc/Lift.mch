MACHINE Lift

VARIABLES  counter

INVARIANT  counter : 0..100 /* NAT */

INITIALISATION counter := 0

OPERATIONS

	inc = PRE counter<100 THEN counter := counter + 1 END ;

	dec = PRE counter>0 THEN counter := counter - 1 END

END
