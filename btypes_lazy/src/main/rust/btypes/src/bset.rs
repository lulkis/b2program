#![ allow( dead_code, non_snake_case) ]

use crate::bboolean::{IntoBool, BBoolean, BBooleanT};
use crate::binteger::{BInt, BInteger};
use crate::bstring::BString;
use crate::bobject::BObject;
use crate::btuple::BTuple;
use crate::orderedhashset::OrderedHashSet as OrdSet;
use crate::lazy_ops::set_ops::setops::{IterWrapper, SetOp, SetOpTraits};
use crate::lazy_ops::set_ops::union::Union;

use std::any::TypeId;
use std::borrow::Borrow;
use std::convert::TryInto;
use im::ordset::Iter;
use std::hash::{Hash, Hasher};
use std::collections::LinkedList;
use std::fmt;
use std::fmt::Debug;
use rand::Rng;

pub trait TBSet: BObject {
    type Item: BObject;

    fn as_ord_set(&self) -> OrdSet<Self::Item>;
    fn as_bset(&self) -> &BSet<Self::Item>;
}

pub trait SetLike: BObject {
    fn get_empty() -> Self;
    fn _union(&self, other: &Self) -> Self;
    fn intersect(&self, other: &Self) -> Self;
}

#[derive(Default, Debug, Eq, PartialOrd, Ord, Clone)]
pub struct BSet<T: BObject> {
    set: OrdSet<T>,
    transformation: Option<Box<dyn SetOp<Item = T>>>,
}

impl<T: BObject> BSet<T> {
    const OP_NAME: &'static str = "set";
}

impl<T: 'static + BObject> SetOp for BSet<T> {
    fn compute(&self, _: &BSet<Self::Item>) -> BSet<Self::Item> {
        match self.transformation.borrow() {
            Some(trans) => trans.compute(&self) , //todo save computation?
            None => self.clone(),
        }
    }

    fn clone_box(&self) -> Box<dyn SetOp<Item=Self::Item>> {
        Box::new(self.clone())
    }

    fn get_op_name(&self) -> &str {
        return BSet::<T>::OP_NAME;
    }

    fn get_rhs(&self) -> Option<Box<dyn SetOp<Item=Self::Item>>> {
        return self.transformation.clone();
    }

    fn to_string(&self, _lhs: Option<&str>) -> String {
        let mut result = "{".to_owned();
        let mut first = true;
        for e in self.set.iter() {
            if !first { result = result + ", " }
            else { first = false; }
            result = result + &format!("{}", e).to_string();
        }
        result = result + "}";
        match &self.transformation {
            Some(op) => return op.to_string(Option::Some(result.as_str())),
            None => return result,
        }
    }
}

impl<T: BObject> SetOpTraits for BSet<T> {
    type Item = T;

    fn iter_lazy<'a>(&'a self, _lhs: &BSet<Self::Item>) -> IterWrapper<T> {
        match &self.transformation {
            Some(op) => op.iter_lazy(self),
            None => IterWrapper::single(self.iter_directly()),
        }
    }

    fn contains_lazy(&self, _lhs: &BSet<Self::Item>, o: &Self::Item) -> bool {
        match &self.transformation {
            Some(op) => op.contains_lazy(self, o),
            None => self.contains_directly(o),
        }
    }

    fn is_empty_lazy(&self, _lhs: &BSet<Self::Item>) -> bool {
        match &self.transformation {
            Some(op) => op.is_empty_lazy(self),
            None => self.is_empty_directly(),
        }
    }

    fn size_lazy(&self, _lhs: &BSet<Self::Item>) -> usize {
        let self_clone = self.clone();
        match self.transformation {
            Some(ref op) => op.size_lazy(&self_clone),
            None => self.size_directly(),
        }
    }
}

impl<I: BObject> Hash for BSet<I> {
    fn hash<H: Hasher>(self: &BSet<I>, state: &mut H) { self.set.hash(state); }
}

impl<I: BObject> PartialEq for BSet<I> {
    fn eq(&self, other: &BSet<I>) -> bool {
        if self.size() != other.size() { return false; }
        for i in self.iter() {
            if !other.contains(i) { return false; }
        }
        return true;
    }
}

impl<T: 'static + BObject> BObject for BSet<T> {}

impl<T: 'static + BObject> TBSet for BSet<T> {
    type Item = T;
    fn as_ord_set(&self) -> OrdSet<Self::Item> { self.set.clone() }
    fn as_bset(&self) -> &BSet<Self::Item> { self }
}

impl<T: 'static + BObject> fmt::Display for BSet<T> {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        return write!(f, "{}", <BSet<T> as SetOp>::to_string(self, Option::None));
    }
}
/*
impl<'a, T: 'static + BObject> IntoIterator for &'a BSet<T>
where
    T: 'a + Ord,
{
    type Item = &'a T;
    type IntoIter = im::ordset::Iter<'a, T>;

    fn into_iter(self) -> Self::IntoIter {
        self.iter()
    }
}
*/
//TODO: check if replacing cache with mutex works and does not impact permormance too much
unsafe impl<T: BObject> Sync for BSet<T> {}

impl<T: BObject> BSet<T> {
    pub fn size(&self) -> usize { self.size_lazy(self) }
    pub fn iter(&self) -> impl Iterator<Item = &T> + '_ { self.iter_lazy(self) }
    pub fn iter_directly(&self) -> Iter<T> { self.set.iter() }
    pub fn contains(&self, o: &T) -> bool { self.contains_lazy(self, o) }
    pub fn contains_directly(&self, o: &T) -> bool { return self.set.contains(o); }
    pub fn size_directly(&self) -> usize { return self.set.len(); }
    pub fn is_empty_directly(&self) -> bool { return self.set.is_empty() }
}

impl<T: 'static + BObject> BSet<T> {

    pub fn new(mut args: Vec<T>) -> BSet<T> {
        let mut ret: BSet<T> = BSet {
            set: OrdSet::new(),
            transformation: Option::None
        };
        while !args.is_empty() {
            ret.set.insert(args.remove(0));
        }
        return ret;
    }

    pub fn fromOrdSet(set: OrdSet<T>) -> BSet<T> {
        return BSet { set: set, transformation: Option::None };
    }

    pub fn get_direct_set(&self) -> Self {
        return BSet { set: self.set.clone(), transformation: Option::None }
    }

    pub fn iter_complete<'a>(&'a self) -> IterWrapper<T> { self.iter_lazy(self) }

    pub fn isEmpty(&self) -> bool { return self.is_empty_lazy(self); }

    pub fn intersect(&self, set: &BSet<T>) -> BSet<T> {
        return BSet{ set: self.set.clone().intersection(set.set.clone()), transformation: Option::None };
    }

    pub fn difference(&self, set: &BSet<T>) -> BSet<T> {
        let result: OrdSet<T> = self.iter().fold(OrdSet::new(), |r_set, element| if set.contains(element) { r_set } else { r_set.update(element.clone()) });
        return BSet::fromOrdSet(result);
    }

    pub fn _union(&self, set: &BSet<T>) -> BSet<T> {
        return BSet { set: self.set.clone(), transformation: Option::Some(Box::new(Union::new(set.clone())))}
    }

    pub fn real_union(&self, set: &BSet<T>) -> BSet<T> {
        return BSet{ set: self.set.clone().union(set.set.clone()), transformation: Option::None };
    }

    pub fn interval(a: &BInteger, b: &BInteger) -> BSet<BInteger> {
        let mut result: BSet<BInteger> = BSet::new(vec![]);
        for i in a.get_val()..b.get_val()+1 {
            result.set.insert(BInteger::new(i));
        }
        return result;
    }

    pub fn card(&mut self) -> BInteger {
        return self._size();
    }

    pub fn _size(&mut self) -> BInteger {
        return BInteger::new(self.size().try_into().unwrap());
    }

    pub fn elementOf(&self, object: &T) -> BBoolean {
        return BBoolean::new(self.contains(object));
    }

    pub fn notElementOf(&self, object: &T) -> BBoolean {
        return BBoolean::new(!self.contains(object));
    }

    pub fn subset(&self, set: &BSet<T>) -> BBoolean {
        return BBoolean::new(self.set.is_subset(&set.set));
    }

    pub fn notSubset(&self, set: &BSet<T>) -> BBoolean {
        return self.subset(set).not();
    }

    pub fn strictSubset(&mut self, set: &mut BSet<T>) -> BBoolean {
        return BBoolean::new(self.size() < set.size() && self.subset(set));
    }

    pub fn strictNotSubset(&mut self, set: &mut BSet<T>) -> BBoolean {
        return BBoolean::new(self.size() >= set.size() || !self.subset(set));
    }

    pub fn fin(&self) -> BSet<BSet<T>> {
        return self.pow();
    }

    pub fn pow(&self) -> BSet<BSet<T>> {
        return self._pow(true);
    }

    pub fn fin1(&self) -> BSet<BSet<T>> {
        return self.pow1();
    }

    pub fn pow1(&self) -> BSet<BSet<T>> {
        return self._pow(false);
    }

    fn _pow(&self, with_empty_set: bool) -> BSet<BSet<T>> {
        let mut result: BSet<BSet<T>> = BSet::new(vec![]);
        let start: BSet<T> = BSet::new(vec![]);
        let mut queue: LinkedList<BSet<T>> = LinkedList::new();
        queue.push_back(start.clone());
        if with_empty_set { result = result._union(&BSet::new(vec![start])); }

        while !queue.is_empty() {
            let current_set: BSet<T> = queue.pop_front().unwrap();
            for element in self.set.iter() {
                let next_set: BSet<T> = current_set._union(&BSet::new(vec![element.clone()]));
                let previous_size = result.size();
                result = result._union(&BSet::new(vec![next_set.clone()]));
                if previous_size < result.size() {
                    queue.push_back(next_set);
                }
            }
        }

        return result;
    }

    pub fn nondeterminism(&self) -> T {
        let mut rng = rand::thread_rng();
        return self.iter().nth(rng.gen_range(0..self.set.len())).unwrap().clone();
    }

    pub fn equal(&self, other: &BSet<T>) -> BBoolean {
        return BBoolean::new(self.eq(other));
    }

    pub fn unequal(&self, other: &BSet<T>) -> BBoolean {
        return BBoolean::new(!self.eq(other));
    }

    pub fn subsetOfInteger(&self) -> BBoolean {
        return BBoolean::new(TypeId::of::<BInteger>() == TypeId::of::<T>());
    }

    pub fn strictSubsetOfInteger(&self) -> BBoolean {
        return self.subsetOfInteger();
    }

    pub fn notSubsetOfInteger(&self) -> BBoolean {
        return self.subsetOfInteger().not();
    }

    pub fn equalInteger(&self) -> BBoolean {
        return BBoolean::new(false);
    }

    pub fn unequalInteger(&self) -> BBoolean {
        return BBoolean::new(true);
    }

    pub fn equalNatural(&self) -> BBoolean {
        return BBoolean::new(false);
    }

    pub fn unequalNatural(&self) -> BBoolean {
        return BBoolean::new(true);
    }

    pub fn equalNatural1(&self) -> BBoolean {
        return BBoolean::new(false);
    }

    pub fn unequalNatural1(&self) -> BBoolean {
        return BBoolean::new(true);
    }

    pub fn equalString(&self) -> BBoolean {
        return BBoolean::new(false);
    }

    pub fn unequalString(&self) -> BBoolean {
        return BBoolean::new(true);
    }

    pub fn equalStruct(&self) -> BBoolean {
        return BBoolean::new(false);
    }

    pub fn unequalStruct(&self) -> BBoolean {
        return BBoolean::new(true);
    }

    pub fn subsetOfString(&self) -> BBoolean {
        return BBoolean::new(TypeId::of::<BString>() == TypeId::of::<T>());
    }

    pub fn strictSubsetOfString(&self) -> BBoolean {
        return self.subsetOfString();
    }

    pub fn notSubsetOfString(&self) -> BBoolean {
        return self.subsetOfString().not();
    }

    pub fn notStrictSubsetOfString(&self) -> BBoolean {
        return self.strictSubsetOfString().not();
    }

    pub fn subsetOfStruct(&self) -> BBoolean {
        return self.is_struct();
    }

    pub fn strictsubsetOfStruct(&self) -> BBoolean {
        return self.subsetOfStruct();
    }

    pub fn notsubsetOfStruct(&self) -> BBoolean {
        return self.subsetOfStruct().not();
    }

    pub fn notStrictsubsetOfStruct(&self) -> BBoolean {
        return self.strictsubsetOfStruct().not();
    }

    //rust specific
    pub fn cartesian<T1: 'static + BObject, T2: 'static + BObject>(set_a: &OrdSet<T1>, set_b: &OrdSet<T2>) -> OrdSet<BTuple<T1, T2>> {
        if set_a.is_empty() || set_b.is_empty() {return OrdSet::<BTuple<T1, T2>>::new();}
        return set_a.iter()
            .fold(OrdSet::<BTuple<T1, T2>>::new(),
                  |set, lhs| set_b.iter().cloned()
                                                         .fold(set,
                                                               |tset, rhs| tset.update(BTuple::new(lhs.clone(), rhs))))
    }
}

impl<T: 'static + BInt> BSet<T> {

    pub fn notStrictSubsetOfInteger(&self) -> BBoolean {
        return self.strictSubsetOfInteger().not();
    }

    pub fn subsetOfNatural(&self) -> BBoolean {
        if self.subsetOfInteger().booleanValue() {
            return BBoolean::new(self.set.iter().find(|i| i.get_binteger_value().isNotNatural().booleanValue()).is_none());
        }
        return BBoolean::new(false);
    }

    pub fn strictSubsetOfNatural(&self) -> BBoolean {
        return self.subsetOfNatural();
    }

    pub fn notSubsetOfNatural(&self) -> BBoolean {
        return self.subsetOfNatural().not();
    }

    pub fn notStrictSubsetOfNatural(&self) -> BBoolean {
        return self.strictSubsetOfNatural().not();
    }

    pub fn subsetOfNatural1(&self) -> BBoolean {
        if self.subsetOfInteger().booleanValue() {
            return BBoolean::new(self.set.iter().find(|i| i.get_binteger_value().isNotNatural1().booleanValue()).is_none());
        }
        return BBoolean::new(false);
    }

    pub fn strictSubsetOfNatural1(&self) -> BBoolean {
        return self.subsetOfNatural();
    }

    pub fn notSubsetOfNatural1(&self) -> BBoolean {
        return self.subsetOfNatural1().not();
    }

    pub fn notStrictSubsetOfNatural1(&self) -> BBoolean {
        return self.strictSubsetOfNatural1().not();
    }

    pub fn _min(self: &BSet<T>) -> BInteger {
        return self.set.get_min().unwrap().get_binteger_value();
    }

    pub fn _max(self: &BSet<T>) -> BInteger {
        return self.set.get_max().unwrap().get_binteger_value();
    }
}

impl<T: 'static + BObject> SetLike for BSet<T> {
    fn get_empty() -> Self { BSet::<T>::new(vec![]) }
    fn _union(&self, other: &Self) -> Self { self._union(other) }
    fn intersect(&self, other: &Self) -> Self { self.intersect(other) }
}

impl<T: 'static + SetLike> BSet<T> {
    pub fn unary__union(&self) -> T {
        return self.iter().fold(T::get_empty(), |result, next| result._union(next));
    }

    pub fn unary_intersect(&self) -> T {
        return self.iter().fold(T::get_empty(), |result, next| result.intersect(next));
    }
}