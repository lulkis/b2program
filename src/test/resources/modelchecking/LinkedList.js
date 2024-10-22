/*
Minor parts of implementation bases on https://kenjj.medium.com/linked-list-with-typescript-ea8c88276e2e
Implementation is strongly modified to imitate behavior of Java linked list, particularly a double-linked list
which can be used as a stack or queue, to access the first and last element (for DFS, BFS, Mixed BFS/DFS) for model checking more efficiently.
The LinkedList implementation is designed for the model checking code generation specifically, thus only contains functions for accessing and modifying the first/last element, and accessing the length efficiently.
*/
export class LinkedListNode {
    constructor(value) {
        this.value = value;
        this.next = null;
        this.prev = null;
    }
}
export class LinkedList {
    constructor() {
        this.head = null;
        this.tail = null;
        this.length = 0;
    }
    pushFront(value) {
        const newNode = new LinkedListNode(value);
        if (this.head === null) {
            this.head = newNode;
            this.tail = newNode;
        }
        else {
            newNode.next = this.head;
            this.head.prev = newNode;
            this.head = newNode;
        }
        this.length = this.length + 1;
    }
    pushBack(value) {
        const newNode = new LinkedListNode(value);
        if (this.tail === null) {
            this.head = newNode;
            this.tail = newNode;
        }
        else {
            newNode.prev = this.tail;
            this.tail.next = newNode;
            this.tail = newNode;
        }
        this.length = this.length + 1;
    }
    popFront() {
        if (this.head === null) {
            return null;
        }
        const frontValue = this.head.value;
        if (this.head === this.tail) {
            this.head = null;
            this.tail = null;
        }
        else {
            this.head = this.head.next;
            this.head.prev = null;
        }
        this.length = this.length - 1;
        return frontValue;
    }
    popBack() {
        if (this.tail === null) {
            return null;
        }
        const backValue = this.tail.value;
        if (this.head === this.tail) {
            this.head = null;
            this.tail = null;
        }
        else {
            this.tail = this.tail.prev;
            this.tail.next = null;
        }
        this.length = this.length - 1;
        return backValue;
    }
    getFront() {
        return this.head !== null ? this.head.value : null;
    }
    getBack() {
        return this.tail !== null ? this.tail.value : null;
    }
    getLength() {
        return length;
    }
}
