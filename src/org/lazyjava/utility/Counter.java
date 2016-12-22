package org.lazyjava.utility;

public class Counter {
	private long cnt = 0;
	
	public Counter() {
		this.cnt = 0;
	}
	
	public Counter(long initValue) {
		this.cnt = initValue;
	}
	
	//----------------------------------------------//
	public long addCount() {
		this.cnt += 1;
		return this.cnt;
	}
	
	public long addCount(long inc) {
		this.cnt += inc;
		return this.cnt;
	}
	
	public long getCount() {
		return this.cnt;
	}
	
	public void setCount(long value) {
		this.cnt = value;
	}
}
