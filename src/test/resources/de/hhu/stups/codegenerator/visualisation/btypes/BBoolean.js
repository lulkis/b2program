"use strict";
export default class BBoolean {
    constructor(value) {
        if (typeof (value) === 'boolean') {
            this.value = value;
        }
        else if (typeof value === 'string') {
            this.value = value.toLowerCase() === 'true';
        }
        else {
            this.value = value.value;
        }
    }
    and(other) {
        return new BBoolean(this.value && other.value);
    }
    or(other) {
        return new BBoolean(this.value || other.value);
    }
    not() {
        return new BBoolean(!this.value);
    }
    implies(other) {
        return this.not().or(other);
    }
    equivalent(other) {
        return this.implies(other).and(other.implies(this));
    }
    equal(other) {
        return new BBoolean(this.value === other.value);
    }
    equals(other) {
        if (other instanceof BBoolean) {
            return false;
        }
        return this.equal(other).booleanValue();
    }
    unequal(other) {
        return new BBoolean(this.value != other.value);
    }
    booleanValue() {
        return this.value;
    }
    toString() {
        return this.value.toString();
    }
    hashCode() {
        return this.value ? 1 : 0;
    }
}
