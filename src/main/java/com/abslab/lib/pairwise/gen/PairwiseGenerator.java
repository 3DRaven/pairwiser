package com.abslab.lib.pairwise.gen;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author i3draven
 *
 *         Pairwise theory test cases generator
 *
 * @param <C> Type of parameters names
 * @param <E> Type of parameters values (or Object and different parameter
 *            types)
 */
@Slf4j
public class PairwiseGenerator<C, E> implements Iterator<List<E>> {

    /**
     * Source data loaded to generator
     */
    private final Map<C, List<E>> baseData;
    /**
     * Index for baseData
     */
    private final PairwiseIndex<C, E> baseDataIndex;
    /**
     * Destination generated cases
     */
    private final Map<C, List<E>> generatedCases;
    /**
     * Current row in destination generated cases generatedCases
     */
    private int currentRow;
    /**
     * Summary generated rows count
     */
    private int rowsCount;

    /**
     * 
     * @param baseData describing of parameters and its possible values
     */
    public PairwiseGenerator(final Map<C, List<E>> baseData) {
        this.baseData = baseData;
        this.baseDataIndex = new PairwiseIndex<>(baseData);
        this.generatedCases = generate();
    }

    /**
     * 
     * @return generated cases map
     */
    public Map<C, List<E>> getGenaratedCases() {
        return Collections.unmodifiableMap(generatedCases);
    }

    /**
     * Generation of test cases
     * 
     * @return
     */
    private Map<C, List<E>> generate() {
        Map<C, List<E>> returnCases = generatedCases;
        if (null == returnCases) {
            baseDataIndex.fillStart();
            // Horizontal growth
            while (!baseDataIndex.isRemovedAll()) {
                log.info("Add column");
                baseDataIndex.addColumn();
                // Vertical growth
                while (baseDataIndex.isNeedRows()) {
                    log.info("Add row");
                    baseDataIndex.addRow();
                }
            }
            baseDataIndex.fillNulls();
            returnCases = baseDataIndex.map(baseData);
            returnCases.entrySet().stream().findAny().ifPresent(r -> rowsCount = r.getValue().size());
        }

        return returnCases;
    }

    @Override
    public boolean hasNext() {
        return currentRow < rowsCount;
    }

    @Override
    public List<E> next() {
        if (currentRow > rowsCount) {
            throw new NoSuchElementException("New rows not found");
        }
        final List<E> row = generatedCases.entrySet().stream().map(e -> e.getValue().get(currentRow))
                .collect(Collectors.toList());
        currentRow++;
        return row;
    }

    /**
     * Return stream of already generated cases
     * 
     * @return
     */
    public Stream<List<E>> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, 0), false);
    }
}
