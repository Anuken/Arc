package io.anuke.arc.util;


import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.IntIntMap;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.function.Function;

import java.util.Comparator;

public class Structs{

    /**Creates an array from the supplied objects.
     * If any arrays are in the list, their contents are added into the list.
     * This is not recursive.*/
    public static <T> Array<T> array(Object... arrays){
        Array<T> result = new Array<>();
        for(Object a : arrays){
            if(a instanceof Array){
                result.addAll((Array<? extends T>) a);
            }else{
                result.add((T)a);
            }
        }
        return result;
    }

    public static IntIntMap mapInt(int... values){
        IntIntMap map = new IntIntMap();
        for(int i = 0; i < values.length; i += 2){
            map.put(values[i], values[i + 1]);
        }
        return map;
    }

    public static <K, V> ObjectMap<K, V> map(Object... values){
        ObjectMap<K, V> map = new ObjectMap<>();

        for(int i = 0; i < values.length / 2; i++){
            map.put((K) values[i * 2], (V) values[i * 2 + 1]);
        }

        return map;
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
