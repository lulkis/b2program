import { BSet } from "https://favu100.github.io/b2program/LandingGear/GearsDoors/btypes/BSet.js";
import { BBoolean } from "https://favu100.github.io/b2program/LandingGear/GearsDoors/btypes/BBoolean.js";
export class BUtils {
}
BUtils.BOOL = new BSet(new BBoolean(true), new BBoolean(false));
export class SelectError extends Error {
    constructor(msg) {
        super(msg);
        Object.setPrototypeOf(this, SelectError.prototype);
    }
}