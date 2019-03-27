package io.anuke.arc.util;


import io.anuke.arc.function.Consumer;
import io.anuke.arc.function.Function;
import io.anuke.arc.function.Predicate;

import java.util.Comparator;

public class Structs{

    /**Uses identity comparisons.*/
    public static <T> boolean contains(T[] array, T value){
        for(T t : array){
            if(t == value) return true;
        }
        return false;
    }

    public static <T> T find(T[] array, Predicate<T> value){
        for(T t : array){
            if(value.test(t)) return t;
        }
        return null;
    }

    public static <T, U> Comparator<T> comparing(Function<? super T, ? extends U> keyExtractor, Comparator<? super U> keyComparator){
        return (c1, c2) -> keyComparator.compare(keyExtractor.get(c1), keyExtractor.get(c2));
    }

    public static <T, U extends Comparable<? super U>> Comparator<T> comparing(Function<? super T, ? extends U> keyExtractor){
        return (c1, c2) -> keyExtractor.get(c1).compareTo(keyExtractor.get(c2));
    }

    public static <T> void each(Consumer<T> cons, T... objects){
        for(T t : objects){
            cons.accept(t);
        }
    }

    public static <T> void forEach(Iterable<T> i, Consumer<T> cons){
        for(T t : i){
            cons.accept(t);
        }
    }

    public static <T> T findMin(T[] arr, Comparator<T> comp){
        T result = null;
        for(T t : arr){
            if(result == null || comp.compare(result, t) < 0){
                result = t;
            }
        }
        return result;
    }

    public static <T> T findMin(T[] arr, Function<T, Integer> proc){
        T result = null;
        int min = Integer.MAX_VALUE;
        for(T t : arr){
            int val = proc.get(t);
            if(val <= min){
                result = t;
                min = val;
            }
        }
        return result;
    }

    public static <T> T findMin(Iterable<T> arr, Comparator<T> comp){
        T result = null;
        for(T t : arr){
            if(result == null || comp.compare(result, t) < 0){
                result = t;
            }
        }
        return result;
    }

    public static <T> boolean inBounds(int x, int y, T[][] array){
        return x >= 0 && y >= 0 && x < array.length && y < array[0].length;
    }

    public static boolean inBounds(int x, int y, int[][] array){
        return x >= 0 && y >= 0 && x < array.length && y < array[0].length;
    }

    public static boolean inBounds(int x, int y, float[][] array){
        return x >= 0 && y >= 0 && x < array.length && y < array[0].length;
    }

    public static boolean inBounds(int x, int y, boolean[][] array){
        return x >= 0 && y >= 0 && x < array.length && y < array[0].length;
    }

    public static <T> boolean inBounds(int x, int y, int z, T[][][] array){
        return x >= 0 && y >= 0 && z >= 0 && x < array.length && y < array[0].length && z < array[0][0].length;
    }

    public static <T> boolean inBounds(int x, int y, int z, int[][][] array){
        return x >= 0 && y >= 0 && z >= 0 && x < array.length && y < array[0].length && z < array[0][0].length;
    }

    public static boolean inBounds(int x, int y, int z, int size, int padding){
        return x >= padding && y >= padding && z >= padding && x < size - padding && y < size - padding
                && z < size - padding;
    }

    public static <T> boolean inBounds(int x, int y, int width, int height){
        return x >= 0 && y >= 0 && x < width && y < height;
    }
}
