MACHINE M_other_node

SEES LibraryLustre

VARIABLES assert_ok, is_initialisation, var_a, var_b

INVARIANT ((assert_ok : BOOL) & ((is_initialisation : BOOL) & ((var_b : LUSTRE_INT) & (var_a : LUSTRE_INT))))

INITIALISATION 
    assert_ok := TRUE; 
    is_initialisation := TRUE; 
    var_a := {}; 
    var_b := {}


OPERATIONS 

    clock_step(param_x) = 
        PRE
            (param_x : LUSTRE_INT)
        THEN
            VAR var_x IN
                var_x := param_x; 
                var_a := var_x; 
                var_b := var_x; 
                is_initialisation := FALSE

            END

        END



END

