package org.luca;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.luca.TableOfPrimes.outerProduct;
import static org.luca.TableOfPrimes.renderToCsv;

class TablesOfPrimesTest {

    @Test
    void theFirstTenPrimesAreComputedCorrectly() {
        var supplier = TableOfPrimes.primeSupplier();
        List<Integer> first10 = Stream.generate(supplier).limit(10).toList();
        assertEquals(List.of(2, 3, 5, 7, 11, 13, 17, 19, 23, 29), first10);
    }

    @Test
    void theSumOfFirst1000PrimesIsComputedCorrectly() {
        var supplier = TableOfPrimes.primeSupplier();
        List<Integer> first1000 = Stream.generate(supplier).limit(1000).toList();
        int sumOfTheFirst1000Primes = first1000.stream().mapToInt(Integer::intValue).sum();
        // Value form Wolfram Alpha
        assertEquals(3682913, sumOfTheFirst1000Primes);
    }

    @Test
    void outerProductSingleEntryIsComputedCorrectly() {
        var v = List.of(1);
        List<List<Integer>> outerProduct = outerProduct(v, Integer::sum);
        assertEquals(List.of(List.of(2)), outerProduct);
    }


    @Test
    void outerProductOrderIsColumnThenRow() {
        var v = List.of(1, 2);
        List<List<Integer>> outerProduct = outerProduct(v, (x, y) -> x - y);
        assertEquals(List.of(List.of(0, -1), List.of(1, 0)), outerProduct);
    }

    @Test
    void renderingForASimpleCase() {
        TableOfPrimes.Table table = new TableOfPrimes.Table(List.of(1, 2), List.of(List.of(3, 4), List.of(5, 6)));
        String rendered = renderToCsv(table, ",");
        String expected = """
                *,1,2
                1,3,4
                2,5,6
                """;
        assertEquals(expected, rendered);
    }

    @Test
    void completeWorkflow5Primes() {
        var supplier = TableOfPrimes.primeSupplier();
        List<Integer> first3 = Stream.generate(supplier).limit(3).toList();
        TableOfPrimes.Table table = new TableOfPrimes.Table(first3, outerProduct(first3, (x, y) -> x * y));
        String rendered = renderToCsv(table, ";");
        String expected = """
                *;2;3;5
                2;4;6;10
                3;6;9;15
                5;10;15;25
                """;
        assertEquals(expected, rendered);
    }

}