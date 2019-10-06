package crystal.util;

import java.util.*;

public class MultiQueue<E> {
	
	public static final List<Integer> levelStats = new ArrayList<Integer>();
	
	private final List<Queue<E>> list = new ArrayList<Queue<E>>();
	private int size = 0;
	private int lastPriority = -1;
	
	public void add(int priority, E e) {
		if (priority < 0) {
			throw new IllegalArgumentException();
		}
		
		for (int i = levelStats.size(); i <= priority; i++) {
			levelStats.add(0);
		}
		
		levelStats.set(priority, levelStats.get(priority) + 1);
		
		if (list.size() <= priority || list.get(priority) == null) {
			for (int i = list.size(); i <= priority; i++) {
				list.add(null);
			}
			
			list.set(priority, new LinkedList<E>());
		}
		
		list.get(priority).add(e);
		size++;
	}
	
	public E remove() {
		if (empty()) {
			throw new NoSuchElementException();
		}
		
		for (int i = 0; i < list.size(); i++) {
			Queue<E> q = list.get(i);
			
			if (q != null && q.size() > 0) {
				size--;
				lastPriority = i;
				return q.remove();
			}
		}
		
		throw new NoSuchElementException();
	}
	
	public int size() {
		return size;
	}
	
	public int getSize(int i) {
		if (list.size() < i || list.get(i) == null) {
			return 0;
		}
		
		return list.get(i).size();
	}
	
	public boolean empty() {
		return size == 0;
	}

	public int lastPriority() {
		return lastPriority;
	}
	
	public void clear() {
		list.clear();
		size = 0;
		lastPriority = -1;
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				s.append(", ");
			}
			
			s.append(list.get(i));
		}
		
		return s.toString();
	}
	
}
