package org.sidiff.bug.localization.common.utilities.java;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class JUtil {
	
	public static <T> Iterable<T> iterable(Supplier<Iterator<T>> iterator) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return iterator.get();
			}
		};
	}

	public static <T> Iterator<T> emptyIterator() {
		return Collections.emptyIterator();
	}
	
	public static <T> Iterator<T> singeltonIterator(T obj) {
		return new Iterator<T>() {
			private boolean hasNext = true;

			public boolean hasNext() {
				return hasNext;
			}

			public T next() {
				if (hasNext) {
					hasNext = false;
					return obj;
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void forEachRemaining(Consumer<? super T> action) {
				Objects.requireNonNull(action);
				if (hasNext) {
					action.accept(obj);
					hasNext = false;
				}
			}
		};
	}
	
	public static void offset(Iterator<?> it, int startIndex) {
		for (int i = 0; i < startIndex; i++) {
			if (it.hasNext()) {
				it.next();
			} else {
				break;
			}
		}
	}
	
	public static boolean contains(Object[] array, Object value) {
		for (Object object : array) {
			if (object.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	public static <K, V> Entry<K, V> getFirstEntryByValue(Map<K, V> map, V value) {
		for (Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue() == value) {
				return entry;
			}
		}
		return null;
	}
	
	
	public static <T> Iterable<T> merge(Collection<T> c1, Collection<T> c2) {
		return () -> Stream.concat(c1.stream(), c2.stream()).iterator();
	}
	
	public static boolean notNull(Object... objects) {
		for (Object object : objects) {
			if (object == null) {
				return false;
			}
		}
		return true;
	}
}
