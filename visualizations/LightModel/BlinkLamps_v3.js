import { BTuple } from './btypes/BTuple.js';
import { BInteger } from './btypes/BInteger.js';
import { BBoolean } from './btypes/BBoolean.js';
import { BRelation } from './btypes/BRelation.js';
import { BSet } from './btypes/BSet.js';
import { SelectError } from "./btypes/BUtils.js";
export var enum_DIRECTIONS;
(function (enum_DIRECTIONS) {
    enum_DIRECTIONS[enum_DIRECTIONS["left_blink"] = 0] = "left_blink";
    enum_DIRECTIONS[enum_DIRECTIONS["right_blink"] = 1] = "right_blink";
    enum_DIRECTIONS[enum_DIRECTIONS["neutral_blink"] = 2] = "neutral_blink";
})(enum_DIRECTIONS || (enum_DIRECTIONS = {}));
export class DIRECTIONS {
    constructor(value) {
        this.value = value;
    }
    equal(other) {
        return new BBoolean(this.value === other.value);
    }
    unequal(other) {
        return new BBoolean(this.value !== other.value);
    }
    equals(o) {
        if (!(o instanceof DIRECTIONS)) {
            return false;
        }
        return this.value === o.value;
    }
    hashCode() {
        return 0;
    }
    toString() {
        return enum_DIRECTIONS[this.value].toString();
    }
    static get_left_blink() { return new DIRECTIONS(enum_DIRECTIONS.left_blink); }
    static get_right_blink() { return new DIRECTIONS(enum_DIRECTIONS.right_blink); }
    static get_neutral_blink() { return new DIRECTIONS(enum_DIRECTIONS.neutral_blink); }
}
export default class BlinkLamps_v3 {
    constructor() {
        BlinkLamps_v3.BLINK_DIRECTION = new BSet(new DIRECTIONS(enum_DIRECTIONS.left_blink), new DIRECTIONS(enum_DIRECTIONS.right_blink));
        BlinkLamps_v3.LAMP_STATUS = new BSet(new BInteger(0), new BInteger(100));
        BlinkLamps_v3.lamp_on = new BInteger(100);
        BlinkLamps_v3.lamp_off = new BInteger(0);
        BlinkLamps_v3.BLINK_CYCLE_COUNTER = BSet.interval(new BInteger(1).negative(), new BInteger(3));
        BlinkLamps_v3.cycleMaxLampStatus = new BRelation(new BTuple(new BBoolean(false), BlinkLamps_v3.lamp_off), new BTuple(new BBoolean(true), BlinkLamps_v3.lamp_on));
        this.active_blinkers = new BSet();
        this.blinkLeft = BlinkLamps_v3.lamp_off;
        this.blinkRight = BlinkLamps_v3.lamp_off;
        this.remaining_blinks = new BInteger(0);
        this.onCycle = new BBoolean(false);
    }
    _copy() {
        var _a, _b, _c;
        let _instance = Object.create(BlinkLamps_v3.prototype);
        for (let key of Object.keys(this)) {
            _instance[key] = (_c = (_b = (_a = this[key])._copy) === null || _b === void 0 ? void 0 : _b.call(_a)) !== null && _c !== void 0 ? _c : this[key];
        }
        return _instance;
    }
    SET_AllBlinkersOff() {
        this.active_blinkers = new BSet();
        this.remaining_blinks = new BInteger(0);
        this.blinkLeft = BlinkLamps_v3.lamp_off;
        this.blinkRight = BlinkLamps_v3.lamp_off;
    }
    SET_AllBlinkersOn() {
        this.active_blinkers = BlinkLamps_v3.BLINK_DIRECTION;
        this.remaining_blinks = new BInteger(1).negative();
        this.blinkLeft = BlinkLamps_v3.cycleMaxLampStatus.functionCall(this.onCycle);
        this.blinkRight = BlinkLamps_v3.cycleMaxLampStatus.functionCall(this.onCycle);
    }
    SET_BlinkersOn(direction, rem) {
        this.active_blinkers = new BSet(direction);
        this.remaining_blinks = rem;
        if ((direction.equal(new DIRECTIONS(enum_DIRECTIONS.right_blink))).booleanValue()) {
            this.blinkLeft = BlinkLamps_v3.lamp_off;
            this.blinkRight = BlinkLamps_v3.cycleMaxLampStatus.functionCall(this.onCycle);
        }
        else {
            this.blinkLeft = BlinkLamps_v3.cycleMaxLampStatus.functionCall(this.onCycle);
            this.blinkRight = BlinkLamps_v3.lamp_off;
        }
    }
    SET_RemainingBlinks(rem) {
        this.remaining_blinks = rem;
    }
    TIME_BlinkerOn() {
        if ((new BBoolean(new BBoolean(this.blinkLeft.equal(BlinkLamps_v3.lamp_off).booleanValue() && this.blinkRight.equal(BlinkLamps_v3.lamp_off).booleanValue()).booleanValue() && this.remaining_blinks.unequal(new BInteger(0)).booleanValue())).booleanValue()) {
            let _ld_remaining_blinks = this.remaining_blinks;
            this.onCycle = new BBoolean(true);
            if ((this.active_blinkers.elementOf(new DIRECTIONS(enum_DIRECTIONS.left_blink))).booleanValue()) {
                this.blinkLeft = BlinkLamps_v3.lamp_on;
            }
            if ((this.active_blinkers.elementOf(new DIRECTIONS(enum_DIRECTIONS.right_blink))).booleanValue()) {
                this.blinkRight = BlinkLamps_v3.lamp_on;
            }
            if ((_ld_remaining_blinks.greater(new BInteger(0))).booleanValue()) {
                this.remaining_blinks = _ld_remaining_blinks.minus(new BInteger(1));
            }
        }
        else {
            throw new SelectError("Parameters are invalid!");
        }
    }
    TIME_BlinkerOff() {
        if ((new BBoolean(this.blinkLeft.equal(BlinkLamps_v3.lamp_off).booleanValue() && this.blinkRight.equal(BlinkLamps_v3.lamp_off).booleanValue()).not()).booleanValue()) {
            this.blinkLeft = BlinkLamps_v3.lamp_off;
            this.blinkRight = BlinkLamps_v3.lamp_off;
            this.onCycle = new BBoolean(false);
            if ((this.remaining_blinks.equal(new BInteger(0))).booleanValue()) {
                this.active_blinkers = new BSet();
            }
        }
        else {
            throw new SelectError("Parameters are invalid!");
        }
    }
    TIME_Nothing(newOnCycle) {
        if ((new BBoolean(new BBoolean(newOnCycle.equal(new BBoolean(false)).booleanValue() && new BBoolean(this.blinkLeft.equal(BlinkLamps_v3.lamp_off).booleanValue() && this.blinkRight.equal(BlinkLamps_v3.lamp_off).booleanValue()).booleanValue()).booleanValue() && this.active_blinkers.equal(new BSet()).booleanValue())).booleanValue()) {
            this.onCycle = newOnCycle;
        }
        else {
            throw new SelectError("Parameters are invalid!");
        }
    }
    _get_BLINK_DIRECTION() {
        return BlinkLamps_v3.BLINK_DIRECTION;
    }
    _get_LAMP_STATUS() {
        return BlinkLamps_v3.LAMP_STATUS;
    }
    _get_lamp_on() {
        return BlinkLamps_v3.lamp_on;
    }
    _get_lamp_off() {
        return BlinkLamps_v3.lamp_off;
    }
    _get_BLINK_CYCLE_COUNTER() {
        return BlinkLamps_v3.BLINK_CYCLE_COUNTER;
    }
    _get_cycleMaxLampStatus() {
        return BlinkLamps_v3.cycleMaxLampStatus;
    }
    _get_active_blinkers() {
        return this.active_blinkers;
    }
    _get_remaining_blinks() {
        return this.remaining_blinks;
    }
    _get_onCycle() {
        return this.onCycle;
    }
    _get_blinkLeft() {
        return this.blinkLeft;
    }
    _get_blinkRight() {
        return this.blinkRight;
    }
    _get__DIRECTIONS() {
        return BlinkLamps_v3._DIRECTIONS;
    }
}
BlinkLamps_v3._DIRECTIONS = new BSet(new DIRECTIONS(enum_DIRECTIONS.left_blink), new DIRECTIONS(enum_DIRECTIONS.right_blink), new DIRECTIONS(enum_DIRECTIONS.neutral_blink));
