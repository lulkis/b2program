MACHINE DefTestComp

VARIABLES
 xx

INVARIANT
  xx: NAT & (xx>1)

INITIALISATION
  xx := 0

OPERATIONS

  inc = BEGIN xx := xx+1 END;
  not_eq(aa) = PRE aa: NAT & (aa>xx or xx>aa) THEN skip END

END
