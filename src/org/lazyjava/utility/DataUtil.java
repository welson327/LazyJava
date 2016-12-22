package org.lazyjava.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DataUtil {
	public static <E> Set<E> toSet(List<E> list) {
		Set<E> set = new HashSet<E>();
		for(E e : list) {
			set.add(e);
		}
		return set;
	}
	
	public static <E> List<E> toList(Set<E> set) {
		List<E> list = new ArrayList<E>();
		Iterator<E> iter = set.iterator();
		while(iter.hasNext()) {
			list.add(iter.next());
		}
		return list;
	}
}
