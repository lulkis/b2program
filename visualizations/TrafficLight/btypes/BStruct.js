import { BBoolean } from "https://favu100.github.io/b2program/visualizations/TrafficLight/btypes/BBoolean.js";
export class BStruct {
    isRecord() {
        return new BBoolean(true);
    }
    isNotRecord() {
        return new BBoolean(false);
    }
}
