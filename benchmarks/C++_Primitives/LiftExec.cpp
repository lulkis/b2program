#include <iostream>
#include <string>
#include "BUtils.cpp"
#include "BInteger.cpp"
#include "BBoolean.cpp"
#include "Lift.cpp"

#ifndef LiftExec_H
#define LiftExec_H

using namespace std;

class LiftExec {

    public:

    private:


        Lift _Lift;


        BInteger counter;

    public:

        LiftExec() {
            counter = (BInteger(0));
        }

        void simulate() {
            while((counter.less((BInteger(10000000)))).booleanValue()) {
                BInteger i;
                i = (BInteger(0));
                while((i.less((BInteger(100)))).booleanValue()) {
                    this->_Lift.inc();
                    i = i.plus((BInteger(1)));
                }
                BInteger _i;
                _i = (BInteger(0));
                while((_i.less((BInteger(100)))).booleanValue()) {
                    this->_Lift.dec();
                    _i = _i.plus((BInteger(1)));
                }
                counter = counter.plus((BInteger(1)));
            }
        }

        BInteger getCounter() {
            BInteger out;
            out = counter;
            return out;
        }

};

int main() {
    LiftExec exec;
    exec.simulate();
    return 0;
}

#endif