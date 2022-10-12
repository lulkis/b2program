#![ allow( dead_code, unused_imports, unused_mut, non_snake_case, non_camel_case_types, unused_assignments ) ]
use std::fmt;
use rand::{thread_rng, Rng};
use btypes::butils;
use btypes::binteger::BInteger;
use btypes::bboolean::BBoolean;
use btypes::brelation::BRelation;
use btypes::bset::BSet;
use btypes::btuple::BTuple;

// regenerate

#[derive(Default, Debug)]
pub struct sort_m2_data1000_asserts {
    j: BInteger,
    k: BInteger,
    l: BInteger,
    g: BRelation<BInteger, BInteger>,
    f: BRelation<BInteger, BInteger>,
    n: BInteger,
}

impl sort_m2_data1000_asserts {

    pub fn new() -> sort_m2_data1000_asserts {
        //values: ''
        let mut m: sort_m2_data1000_asserts = Default::default();
        m.init();
        return m;
    }
    fn init(&mut self) {
        self.g = self.f.clone().clone();
        self.k = BInteger::new(1);
        self.l = BInteger::new(1);
        self.j = BInteger::new(1);
        let mut _ic_set_0 = BRelation::<BInteger, BInteger>::new(vec![]);
        for _ic_i_1 in BSet::<BInteger>::interval(&BInteger::new(1), &BInteger::new(1000)).clone().iter().cloned() {
            _ic_set_0 = _ic_set_0._union(&BRelation::<BInteger, BInteger>::new(vec![BTuple::new(_ic_i_1, BInteger::new(1500).minus(&_ic_i_1))]));

        }
        self.f = _ic_set_0;
        self.n = BInteger::new(1000);
    }

    pub fn get_f(&self) -> BRelation<BInteger, BInteger> {
        return self.f.clone();
    }

    pub fn get_n(&self) -> BInteger {
        return self.n.clone();
    }

    pub fn get_j(&self) -> BInteger {
        return self.j.clone();
    }

    pub fn get_k(&self) -> BInteger {
        return self.k.clone();
    }

    pub fn get_l(&self) -> BInteger {
        return self.l.clone();
    }

    pub fn get_g(&self) -> BRelation<BInteger, BInteger> {
        return self.g.clone();
    }

    pub fn progress(&mut self) -> () {
        if (self.k.unequal(&self.n).and(&self.j.equal(&self.n))).booleanValue() {
            let mut _ld_g = self.g.clone();
            let mut _ld_k = self.k.clone();
            let mut _ld_l = self.l.clone();
            self.g = _ld_g._override(&BRelation::new(vec![BTuple::from_refs(&_ld_k, &_ld_g.functionCall(&_ld_l))])._override(&BRelation::new(vec![BTuple::from_refs(&_ld_l, &_ld_g.functionCall(&_ld_k))]))).clone().clone();
            self.k = _ld_k.plus(&BInteger::new(1));
            self.j = _ld_k.plus(&BInteger::new(1));
            self.l = _ld_k.plus(&BInteger::new(1));
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }

    pub fn prog1(&mut self) -> () {
        if (self.k.unequal(&self.n).and(&self.j.unequal(&self.n)).and(&self.g.functionCall(&self.l).lessEqual(&self.g.functionCall(&self.j.plus(&BInteger::new(1)))))).booleanValue() {
            let mut _ld_j = self.j.clone();
            let mut _ld_l = self.l.clone();
            self.l = _ld_l;
            self.j = _ld_j.plus(&BInteger::new(1));
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }

    pub fn prog2(&mut self) -> () {
        if (self.k.unequal(&self.n).and(&self.j.unequal(&self.n)).and(&self.g.functionCall(&self.l).greater(&self.g.functionCall(&self.j.plus(&BInteger::new(1)))))).booleanValue() {
            let mut _ld_j = self.j.clone();
            self.j = _ld_j.plus(&BInteger::new(1));
            self.l = _ld_j.plus(&BInteger::new(1));
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }

    pub fn final_evt(&mut self) -> () {
        if (self.k.equal(&self.n)).booleanValue() {
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }
}

