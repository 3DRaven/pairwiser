package com.abslab.lib.pairwise.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 * Index Class for generation of test cases
 *
 * @author i3draven
 *
 * @param <C> type of parameters names
 * @param <E> type of values of parameters
 */
@Slf4j
public class PairwiseIndex<C, E> {

    // Index for base parameters values
    private PrettyPrintedMap<Integer, List<Integer>> sortedIndex;
    private List<Integer> indexKeys;
    // Last right column index number
    private int rightColumnName;
    private final PrettyPrintedMap<Pair<Integer>, List<Pair<Integer>>> allPossiblePairs = new PrettyPrintedMap<>(
            new HashMap<>());
    private final PrettyPrintedMap<Pair<Integer>, Set<Pair<Integer>>> removedPairs = new PrettyPrintedMap<>(
            new HashMap<>());
    private int removedPairsCount;
    private Set<Pair<Integer>> finalPairwiseIndexColumns;
    private final PrettyPrintedMap<Integer, List<Integer>> finalPairwiseIndex = new PrettyPrintedMap<>(
            new LinkedHashMap<>());

    public PairwiseIndex(final Map<C, List<E>> baseData) {
        createIndex(baseData);
        generateAllPairs();
    }

    /**
     * Make data index for base params values
     *
     * @param baseData base params real values
     * @return inner class {@link PrettyPrintedMap} contains {@link Map} with
     *         indexed param names and values
     */
    private void createIndex(final Map<C, List<E>> baseData) {
        log.info("Start createIndex()");

        final ArrayList<C> baseDataColumnNames = new ArrayList<>(baseData.keySet());

        PrettyPrintedMap<Integer, List<Integer>> index = new PrettyPrintedMap<>(new LinkedHashMap<>());

        for (int i = 0; i < baseData.size(); i++) {
            index.put(i, Collections.unmodifiableList(IntStream
                    .range(0, baseData.get(baseDataColumnNames.get(i)).size()).boxed().collect(Collectors.toList())));
        }

        sortedIndex = index.getSortedByRowSize();
        indexKeys = Collections.unmodifiableList(new ArrayList<>(sortedIndex.keySet()));

        log.debug("Index is created {}", sortedIndex);
    }

    @Override
    public String toString() {
        return finalPairwiseIndex.toString();
    }

    /**
     * Just fill empty values to any because we cover all need pairs
     */

    public void fillNulls() {
        log.info("Start fillNulls()");
        log.debug("Final index state  {}", finalPairwiseIndex);
        finalPairwiseIndex.entrySet().stream().map(Entry::getValue).forEach(c -> {
            for (int i = 0; i < c.size(); i++) {
                if (null == c.get(i)) {
                    c.set(i, sortedIndex.get(0).get(0));
                }
            }
        });

        log.debug("Final pairwise index {}", finalPairwiseIndex);
    }

    /**
     * Add all pairs of first two columns to cases because we need all this pairs in
     * cases anyway
     */
    public void fillStart() {
        final int firstColumnName = addColumnToRight();
        final int secondColumnName = addColumnToRight();
        final Pair<Integer> addedColumns = Pair.of(firstColumnName, secondColumnName);
        final List<Pair<Integer>> allPairs = getAllPairsOfColumn(addedColumns);
        allPairs.stream().forEach(p -> addPairToRow(addedColumns, p));
    }

    /**
     * Generate of real cases from index and base params values
     *
     * @param baseData base params values
     * @return {@link Map} of pairwise test cases with real values of params where
     *         Map key is param name, and "columns" is {@link List} of case values
     */
    public Map<C, List<E>> map(final Map<C, List<E>> baseData) {
        log.info("Start map()");
        if (getNotRemovedPairs().entrySet().stream().mapToInt(e -> e.getValue().size()).sum() != 0) {
            throw new IllegalStateException("Not all pairs covered");
        }

        log.info("All pairs covered");

        final ArrayList<C> baseDataKeys = new ArrayList<>(baseData.keySet());

        final PrettyPrintedMap<C, List<E>> cases = new PrettyPrintedMap<>(new LinkedHashMap<>());
        for (int i = 0; i < sortedIndex.size(); i++) {
            if (finalPairwiseIndex.containsKey(i)) {
                for (final Integer indexValue : finalPairwiseIndex.get(i)) {
                    final C baseColumn = baseDataKeys.get(i);
                    final E baseValue = baseData.get(baseColumn).get(indexValue);
                    cases.computeIfAbsent(baseColumn, k -> new ArrayList<>()).add(baseValue);
                }
            } else {
                log.warn("This column is not in generated set {}", baseDataKeys.get(i));
            }
        }

        log.debug("Cases generated {}", cases);

        final int size = finalPairwiseIndex.getMaxColumnSize();

        if (cases.entrySet().stream().map(c -> c.getValue().size()).anyMatch(s -> !s.equals(size))) {
            log.error("Cases generated {}", cases);
            throw new IllegalStateException(String.format("We have broken column in index %s", finalPairwiseIndex));
        }

        log.info("Number of generated cases: {}",
                Optional.ofNullable(finalPairwiseIndex.get(indexKeys.get(0))).map(List::size).orElse(0));
        return cases;

    }

    /**
     * Add column to the right position of index
     *
     * @return index number of added column
     */
    public Integer addColumnToRight() {
        rightColumnName = indexKeys.get(finalPairwiseIndex.size());
        if (!sortedIndex.get(rightColumnName).isEmpty()) {
            finalPairwiseIndex.put(rightColumnName, new ArrayList<>());
            finalPairwiseIndexColumns = calculateColumnsPairs(finalPairwiseIndex.keySet());
            return rightColumnName;
        } else {
            throw new IllegalArgumentException(String.format("We have empty column [%d] for cover", rightColumnName));
        }
    }

    /**
     * Horizontal growth
     */
    public void addColumn() {
        addColumnToRight();
        List<Integer> columnCandidates = new ArrayList<>(getCandidatesToRight());

        // Сначала добавим всех кандидатов по очереди без проверок
        for (final Integer v : columnCandidates) {
            addValueToRight(v);
        }

        // Расширяем наш набор тестов справа
        for (int i = columnCandidates.size(); i < finalPairwiseIndex.getMaxColumnSize(); i++) {
            final Integer value = columnCandidates.stream().max(this::compareCandidates)
                    .orElseThrow(() -> new IllegalStateException("Unable to found candidate to adding"));
            columnCandidates.remove(value);
            // Это не обязательно делать, просто повышает разнообразие параметров в тестах
            if (columnCandidates.isEmpty()) {
                columnCandidates = new ArrayList<>(getCandidatesToRight());
            }
            addValueToRight(value);
        }

        log.trace("After column adding state {}", finalPairwiseIndex);
    }

    /**
     * Add one row value to current right column
     *
     * @param value index value to adding
     * @throws IndexOutOfBoundsException when column size is greater than length of
     *                                   first column of index (index sorted from
     *                                   max column size to min)
     */
    public void addValueToRight(final Integer value) {
        final List<Integer> c = finalPairwiseIndex.get(rightColumnName);
        if (c.size() < finalPairwiseIndex.getMaxColumnSize()) {
            c.add(value);
        } else {
            throw new IndexOutOfBoundsException(String.format("We have maximum of rows:%n%s", sortedIndex));
        }
        // Added row number
        final int row = c.size() - 1;

        addAllRemovedPairs(rightColumnName, row);
    }

    /**
     *
     * @param columnName pair of params names used as column name to get list of
     *                   possible pairs values for this params pair
     * @return list of possible values for this params
     */
    public List<Pair<Integer>> getAllPairsOfColumn(final Pair<Integer> columnName) {
        return allPossiblePairs.get(columnName);
    }

    /**
     * Make decision to make new row (new case for tests) to cover all possible
     * pairs in already covered pairs of parameters. So if we cover part of possible
     * pairs by Horizontal Growth we can cover all other by Vertical growth
     *
     * @return true if need to add new test case
     */
    public boolean isNeedRows() {
        final int allNotRemoved = allPossiblePairs.entrySet().stream().filter(e -> removedPairs.containsKey(e.getKey()))
                .mapToInt(e -> e.getValue().size()).sum();
        return removedPairsCount != allNotRemoved;
    }

    /**
     *
     * @return true if removed all pairs
     */
    public boolean isRemovedAll() {
        final int allPairsCount = allPossiblePairs.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
        return removedPairsCount == allPairsCount;
    }

    /**
     * Add all pairs in row to removed. Will been added only not null pairs of
     * values
     *
     * @param row    index of row
     * @param column index of column
     */
    private void addAllRemovedPairs(final int column, final int row) {
        finalPairwiseIndexColumns.stream().filter(p -> p.getSecond().equals(column)).forEach(p -> {
            final List<Integer> firstValues = finalPairwiseIndex.get(p.getFirst());
            final List<Integer> secondValues = finalPairwiseIndex.get(p.getSecond());
            final Integer firstValue = firstValues.get(row);
            final Integer secondValue = secondValues.get(row);
            if (null != firstValue && null != secondValue) {
                if (removedPairs.computeIfAbsent(p, k -> new HashSet<>())
                        .add(Pair.of(firstValues.get(row), secondValues.get(row)))) {
                    removedPairsCount++;
                }
            } else {
                log.trace("Skipt null values pair");
            }
        });
    }

    /**
     * Gets the number of pairs to be deleted and not deleted if value will added to
     * row
     *
     * @param value for adding
     * @return {@link Pair} with - first value is number of removed pairs with this
     *         value and second is number of not removed pairs with this value
     */
    private Pair<Integer> getRemovedPairs(final Integer value) {
        final AtomicInteger removed = new AtomicInteger(0);
        final AtomicInteger notRemovedInColumns = new AtomicInteger(0);

        // Select pairs only with right column, al other pairs already removed
        finalPairwiseIndexColumns.stream().filter(p -> p.getSecond().equals(rightColumnName)).forEach(p -> {
            if (!isRemoved(p, Pair.of(getValue(getRightColumnRow(), p.getFirst()), value))) {
                removed.incrementAndGet();
                notRemovedInColumns.addAndGet(countNotRemoved(p));
            }
        });
        return Pair.of(removed.get(), notRemovedInColumns.get());
    }

    /**
     * Get value from column
     *
     * @param row        row index
     * @param columnName column index
     * @return
     */
    private Integer getValue(final Integer row, final Integer columnName) {
        final List<Integer> values = finalPairwiseIndex.get(columnName);
        return values.get(row);
    }

    /**
     *
     * @return max right column current size in index
     */
    private int getRightColumnRow() {
        return finalPairwiseIndex.get(rightColumnName).size();
    }

    /**
     * Make decision between two possible candidate values to adding to column. If
     * candidates is equals then will be compared not removed pairs of two
     * candidates. Will be used candidate with less not removed pairs.
     *
     * @param c1 candidate index
     * @param c2 candidate index
     * @return The value 0 if this equals candidates, 1 if c1 removed more pairs
     *         than c2, -1 if c2 removed more pairs than c1
     */
    public int compareCandidates(final Integer c1, final Integer c2) {

        if (c1.equals(c2)) {
            return 0;
        }

        final Pair<Integer> rc1 = getRemovedPairs(c1);
        final Pair<Integer> rc2 = getRemovedPairs(c2);

        // If first candidate will be removed more pairs
        if (rc1.getFirst() > rc2.getFirst()) {
            return 1;
        } else if (rc1.getFirst() == rc2.getFirst()) {
            // Compare to not removed pairs counters of candidates
            if (rc1.getSecond() < rc2.getSecond()) {
                return -1;
            } else if (rc1.getSecond() == rc2.getSecond()) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return -1;
        }
    }

    /**
     *
     * @param columnName pair of columns to be checked
     * @param pair       pair of values to be checked
     * @return true if removed
     */
    private boolean isRemoved(final Pair<Integer> columnName, final Pair<Integer> pair) {
        return removedPairs.containsKey(columnName) && removedPairs.get(columnName).contains(pair);
    }

    /**
     *
     * @param columnName pair of column indexes
     * @return number of not removed pairs
     */
    private int countNotRemoved(final Pair<Integer> columnName) {
        return (int) allPossiblePairs.get(columnName).stream().filter(p -> !isRemoved(columnName, p)).count();
    }

    /**
     *
     * @return {@link List} of possible values to right column
     */
    public List<Integer> getCandidatesToRight() {
        return sortedIndex.get(rightColumnName);
    }

    public PrettyPrintedMap<Pair<Integer>, Set<Pair<Integer>>> getNotRemovedPairs() {
        final PrettyPrintedMap<Pair<Integer>, Set<Pair<Integer>>> notRemovedPairs = new PrettyPrintedMap<>(
                new HashMap<>());
        removedPairs.entrySet().stream()
                .forEach(e -> allPossiblePairs.get(e.getKey()).stream().filter(v -> !e.getValue().contains(v))
                        .forEach(v -> notRemovedPairs.computeIfAbsent(e.getKey(), k -> new HashSet<>()).add(v)));
        return notRemovedPairs;
    }

    /**
     * Add new row to index with values from param values and null as other values
     *
     * @param columnsNames pair of columns index to set values from values param
     * @param values       values to set
     */
    public void addPairToRow(@NonNull final Pair<Integer> columnsNames, @NonNull final Pair<Integer> values) {
        for (final Entry<Integer, List<Integer>> column : finalPairwiseIndex.entrySet()) {
            final Integer key = column.getKey();
            final List<Integer> value = column.getValue();
            if (key.equals(columnsNames.getFirst())) {
                value.add(values.getFirst());
            } else if (key.equals(columnsNames.getSecond())) {
                value.add(values.getSecond());
            } else {
                value.add(null);
            }
        }

        addAllRemovedPairs(columnsNames.getSecond(), finalPairwiseIndex.get(columnsNames.getSecond()).size() - 1);
    }

    /**
     * Add new row to cases when we need to cover pairs not covered in Horizontal
     * growth
     */
    public void addRow() {
        final Map<Pair<Integer>, Set<Pair<Integer>>> notRemoved = getNotRemovedPairs();

        // Check all columns with not removed pairs
        notRemoved.entrySet().stream().forEach(c -> {
            // Get all not removed pairs from column
            for (final Pair<Integer> pair : c.getValue()) {
                final List<Integer> firstColumn = finalPairwiseIndex.get(c.getKey().getFirst());
                final List<Integer> secondColumn = finalPairwiseIndex.get(c.getKey().getSecond());
                boolean found = false;
                // We used second column because columns is sorten by length and second shorter
                // or equal to first
                for (int i = 0; i < secondColumn.size(); i++) {
                    final Integer firstValue = firstColumn.get(i);
                    final Integer secondValue = secondColumn.get(i);
                    if (null == firstValue && null != secondValue && secondValue.equals(pair.getSecond())) {
                        // If we found this not removed pair in test cases and first value is null
                        // (possible any value) then just fill this pair
                        firstColumn.set(i, pair.getFirst());
                        addPairToRemoved(c.getKey(), pair);
                        found = true;
                        break;
                    }

                    if (null == firstValue && null == secondValue) {
                        // if we found "Just empty" position, we just will fill it
                        firstColumn.set(i, pair.getFirst());
                        secondColumn.set(i, pair.getSecond());
                        addPairToRemoved(c.getKey(), pair);
                        found = true;
                        break;
                    }
                }

                // If pair for filling is not found then need to add new row with this pair as
                // value for this columns and other position values as null
                if (!found) {
                    firstColumn.add(pair.getFirst());
                    secondColumn.add(pair.getSecond());
                    addPairToRemoved(c.getKey(), pair);
                    for (int j = 0; j < finalPairwiseIndex.size(); j++) {
                        final Integer columnName = indexKeys.get(j);
                        if (columnName != c.getKey().getFirst() && columnName != c.getKey().getSecond()) {
                            finalPairwiseIndex.get(columnName).add(null);
                        }
                    }
                }
            }
        });

        log.trace("After row adding state {}", finalPairwiseIndex);
    }

    /**
     *
     * @param columnName indexes of two columns
     * @param pair       removed values of this columns
     */
    private void addPairToRemoved(final Pair<Integer> columnName, final Pair<Integer> pair) {
        if (removedPairs.computeIfAbsent(columnName, k -> new HashSet<>()).add(pair)) {
            removedPairsCount++;
        }
    }

    /**
     * Cartesian products of all pairs of columns for pairwise
     */
    private void generateAllPairs() {
        log.info("Start generateAllPairs()");
        final Set<Pair<Integer>> columnPairs = calculateColumnsPairs(sortedIndex.keySet());

        columnPairs.stream().forEach(p -> {

            final List<Integer> firstColumn = sortedIndex.get(p.getFirst());
            final List<Integer> secondColumn = sortedIndex.get(p.getSecond());

            // Columns is sorted by length, so we can just get values from first to second
            // Pairs of columns used as example 1,2,3 columns so pairs 1-2 1-3 2-3
            final List<Pair<Integer>> pairs = new ArrayList<>();
            for (final Integer element : firstColumn) {
                for (final Integer element2 : secondColumn) {
                    pairs.add(Pair.of(element, element2));
                }
            }
            allPossiblePairs.put(p, Collections.unmodifiableList(pairs));
        });

        log.trace("All pairs is generated {}", allPossiblePairs);
    }

    /**
     *
     * @return all possible pairs of columns
     */
    private static Set<Pair<Integer>> calculateColumnsPairs(final Set<Integer> columnNames) {
        final Set<Pair<Integer>> possiblePairs = new HashSet<>();

        final List<Integer> keys = new ArrayList<>(columnNames);

        for (int i = 0; i < columnNames.size(); i++) {
            for (int j = i + 1; j < columnNames.size(); j++) {
                possiblePairs.add(Pair.of(keys.get(i), keys.get(j)));
            }
        }
        return Collections.unmodifiableSet(possiblePairs);
    }

    /**
     * Utility class to implement Pair logic
     *
     * @author i3draven
     *
     * @param <E> type of values
     */
    @Value
    public static class Pair<E> {
        private E first;
        private E second;

        /**
         * Fabric method to make new pair
         *
         * @param <E> type of values
         * @param f   first value
         * @param s   second value
         * @return
         */
        public static <E> Pair<E> of(final E f, final E s) {
            return new Pair<>(f, s);
        }

        @Override
        public String toString() {
            return "[" + Objects.toString(first, "-") + "," + Objects.toString(second, "-") + "]";
        }
    }

    /**
     * Delegate class of map with pretty printed toString for comfortable debugging.
     * This is "Array with columns" abstraction
     *
     * @author i3draven
     *
     * @param <C> column name types
     * @param <E> values types
     */
    @RequiredArgsConstructor
    public static class PrettyPrintedMap<C, E extends Collection<?>> implements Map<C, E> {

        @Delegate
        private final Map<C, E> backMap;

        /**
         * Sort this array of columns by column size
         *
         * @return new sorted {@link LinkedHashMap}
         */
        public PrettyPrintedMap<C, E> getSortedByRowSize() {
            return new PrettyPrintedMap<>(backMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue((v1, v2) -> -Integer.compare(v1.size(), v2.size())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                            LinkedHashMap::new)));
        }

        /**
         *
         * @return size max size of columns in this array of columns
         */
        public int getMaxColumnSize() {
            return backMap.entrySet().stream().mapToInt(c -> c.getValue().size()).max().orElse(0);
        }

        @Override
        public String toString() {
            // From java 1.5 it is optimized to StringBuilder by compiler :)
            String pretty = "{\n";
            pretty += backMap.entrySet().stream().map(column -> {
                String res = " ";
                res += Objects.toString(column.getKey());
                res += " = [";
                res += column.getValue().stream().map(v -> Objects.toString(v, "-")).collect(Collectors.joining(","));
                res += "]";
                return res;
            }).collect(Collectors.joining(",\n"));
            pretty += "\n}";
            return pretty;
        }
    }

}
