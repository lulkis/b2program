#![ allow( dead_code, unused_imports, unused_mut, non_snake_case, non_camel_case_types, unused_assignments ) ]
use std::fmt;
use rand::{thread_rng, Rng};
use btypes::butils;
use btypes::bboolean::BBoolean;
use btypes::bset::BSet;
use btypes::bobject::BObject;


#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub enum colors {
    red, 
    redyellow, 
    yellow, 
    green
}
impl colors {
    pub fn equal(&self, other: &colors) -> BBoolean { BBoolean::new(*self == *other)}
    pub fn unequal(&self, other: &colors) -> BBoolean { BBoolean::new(*self != *other)}
}
impl BObject for colors {}
impl Default for colors {
    fn default() -> Self { colors::red }
}
impl fmt::Display for colors {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
       match *self {
           colors::red => write!(f, "red"),
           colors::redyellow => write!(f, "redyellow"),
           colors::yellow => write!(f, "yellow"),
           colors::green => write!(f, "green"),
       }
    }
}

#[derive(Default, Debug)]
pub struct TrafficLight {
    tl_cars: colors,
    tl_peds: colors,
    _colors: BSet<colors>,
}

impl TrafficLight {

    pub fn new() -> TrafficLight {
        //values: ''
        let mut m: TrafficLight = Default::default();
        m.init();
        return m;
    }
    fn init(&mut self) {
        self._colors = BSet::new(vec![colors::red, colors::redyellow, colors::yellow, colors::green]);
        self.tl_cars = colors::red;
        self.tl_peds = colors::red;
    }

    pub fn get_tl_cars(&self) -> colors {
        return self.tl_cars.clone();
    }

    pub fn get_tl_peds(&self) -> colors {
        return self.tl_peds.clone();
    }

    pub fn get__colors(&self) -> BSet<colors> {
        return self._colors.clone();
    }

    pub fn cars_ry(&mut self) -> () {
        if (self.tl_cars.equal(&colors::red).and(&self.tl_peds.equal(&colors::red))).booleanValue() {
            self.tl_cars = colors::redyellow;
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }

    pub fn cars_y(&mut self) -> () {
        if (self.tl_cars.equal(&colors::green).and(&self.tl_peds.equal(&colors::red))).booleanValue() {
            self.tl_cars = colors::yellow;
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }

    pub fn cars_g(&mut self) -> () {
        if (self.tl_cars.equal(&colors::redyellow).and(&self.tl_peds.equal(&colors::red))).booleanValue() {
            self.tl_cars = colors::green;
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }

    pub fn cars_r(&mut self) -> () {
        if (self.tl_cars.equal(&colors::yellow).and(&self.tl_peds.equal(&colors::red))).booleanValue() {
            self.tl_cars = colors::red;
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }

    pub fn peds_r(&mut self) -> () {
        if (self.tl_peds.equal(&colors::green).and(&self.tl_cars.equal(&colors::red))).booleanValue() {
            self.tl_peds = colors::red;
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }

    pub fn peds_g(&mut self) -> () {
        if (self.tl_peds.equal(&colors::red).and(&self.tl_cars.equal(&colors::red))).booleanValue() {
            self.tl_peds = colors::green;
        } else {
            panic!("ERROR: called SELECT-function with incompatible parameters!");
        }
    }
}

