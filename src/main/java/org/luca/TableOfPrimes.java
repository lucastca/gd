package org.luca;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableOfPrimes {

    // A table is represented by a header section and a content section
    // the content section is stored in a List<List> using a row-major convention
    public record Table(List<Integer> headers, List<List<Integer>> content) {
    }

    // Creates a textual representation of a table
    public static String renderToCsv(Table t, String delimiter) {
        var sb = new StringBuilder();
        // Render header
        sb.append("*");
        sb.append(delimiter);
        sb.append(t.headers.stream().map(Object::toString).collect(Collectors.joining(delimiter)));
        sb.append(System.lineSeparator());
        // Render content
        Iterator<Integer> columnHeaderIterator = t.headers.iterator();
        for (var row : t.content) {
            sb.append(columnHeaderIterator.next());
            sb.append(delimiter);
            sb.append(row.stream().map(Object::toString).collect(Collectors.joining(delimiter)));
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    // Returns a prime numbers generating supplier
    public static Supplier<Integer> primeSupplier() {
        List<Integer> primes = new ArrayList<>();
        // Using a single element array as Java does not allow lambda capture by reference for
        // primitive types
        final int[] nextN = {1};
        return () -> {
            while (nextN[0]++ > 0) {
                // According to the definition a prime number is not divisible by any other prime
                // optimization: we can only check up to square root of n
                boolean isPrime = primes.stream().takeWhile(n -> n * 2 <= nextN[0]).noneMatch(u -> nextN[0] % u == 0);
                if (isPrime) {
                    primes.add(nextN[0]);
                    return nextN[0];
                }
            }
            return 0;
        };
    }

    // Generates the table content
    // this operation can be thought as computing the outer product of a vector and itself
    // the reducing operation is parametrized
    public static <T, U> List<List<U>> outerProduct(Collection<T> vector, BiFunction<T, T, U> function) {
        return vector.stream().map(n -> vector.stream().map(m -> function.apply(n, m)).toList()).toList();

    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("TableOfPrimes").build().defaultHelp(true).description("Generates the multiplication table for the first n primes," + "must be between 1 and 1000");
        parser.addArgument("-n").help("number of primes to be generated").type(Integer.class).choices(Arguments.range(1, 1000)).required(true);
        parser.addArgument("-d").help("output file value delimiter, defaults to \",\"").type(String.class).setDefault(",").required(false);
        try {
            Namespace ns = parser.parseArgs(args);
            int n = ns.getInt("n");
            String delimiter = ns.getString("d");
            Supplier<Integer> primeSupplier = primeSupplier();
            List<Integer> firstNPrimes = Stream.generate(primeSupplier).limit(n).toList();
            Table table = new Table(firstNPrimes, outerProduct(firstNPrimes, (x, y) -> x * y));
            String renderedTable = renderToCsv(table, delimiter);
            System.out.println(renderedTable);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

    }
}