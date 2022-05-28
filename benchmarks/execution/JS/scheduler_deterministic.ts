import {BBoolean} from './btypes/BBoolean.js';
import {BSet} from './btypes/BSet.js';
import {BObject} from './btypes/BObject.js';
import {BUtils} from "./btypes/BUtils.js";

export enum enum_PID {
    process1,
    process2,
    process3
}

export class PID implements BObject{
    value: enum_PID;

    constructor(value: enum_PID) {
        this.value = value;
    }

    equal(other: PID) {
        return new BBoolean(this.value === other.value);
    }

    unequal(other: PID) {
        return new BBoolean(this.value !== other.value);
    }

    equals(o: any) {
        if(!(o instanceof PID)) {
            return false;
        }
        return this.value === o.value;
    }

    hashCode() {
        return 0;
    }

    toString() {
        return enum_PID[this.value].toString();
    }

    static get_process1 () {return new PID(enum_PID.process1);}
    static get_process2 () {return new PID(enum_PID.process2);}
    static get_process3 () {return new PID(enum_PID.process3);}


}


export default class scheduler_deterministic {



    private static _PID: BSet<PID> = new BSet(new PID(enum_PID.process1), new PID(enum_PID.process2), new PID(enum_PID.process3));

    private active: BSet<PID>;
    private _ready: BSet<PID>;
    private waiting: BSet<PID>;

    constructor() {
        this.active = new BSet();
        this._ready = new BSet();
        this.waiting = new BSet();
    }

     _new(pp: PID): void {
        if((new BBoolean(new BBoolean(scheduler_deterministic._PID.elementOf(pp).booleanValue() && this.active.notElementOf(pp).booleanValue()).booleanValue() && this._ready.union(this.waiting).notElementOf(pp).booleanValue())).booleanValue()) {
            this.waiting = this.waiting.union(new BSet(pp));
        } 
    }

     del(pp: PID): void {
        if((this.waiting.elementOf(pp)).booleanValue()) {
            this.waiting = this.waiting.difference(new BSet(pp));
        } 
    }

     ready(rr: PID): void {
        if((this.waiting.elementOf(rr)).booleanValue()) {
            this.waiting = this.waiting.difference(new BSet(rr));
            if((this.active.equal(new BSet())).booleanValue()) {
                this.active = new BSet(rr);
            } else {
                this._ready = this._ready.union(new BSet(rr));
            }
        } 
    }

     swap(pp: PID): void {
        if((this.active.unequal(new BSet())).booleanValue()) {
            this.waiting = this.waiting.union(this.active);
            if((this._ready.equal(new BSet())).booleanValue()) {
                this.active = new BSet();
            } else {
                this.active = new BSet(pp);
                this._ready = this._ready.difference(new BSet(pp));
            }
        } 
    }

    _get_active(): BSet<PID> {
        return this.active;
    }

    _get__ready(): BSet<PID> {
        return this._ready;
    }

    _get_waiting(): BSet<PID> {
        return this.waiting;
    }

    _get__PID(): BSet<PID> {
        return scheduler_deterministic._PID;
    }


}

