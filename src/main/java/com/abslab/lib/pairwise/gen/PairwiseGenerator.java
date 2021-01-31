/*******************************************************************************
 * Copyright 2021 Renat Eskenin
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.abslab.lib.pairwise.gen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.Getter;
import lombok.NonNull;
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
    @Getter
    private int rowsCount;

    /**
     *
     * @param i test case number
     * @return Map with this test case where key is param name and value is param
     *         value
     */
    public Map<C, E> getTestCase(int i) {
        if (i >= rowsCount) {
            throw new IndexOutOfBoundsException(String.format("We do not have this test [%d]", i));
        }

        Map<C, E> result = new HashMap<>();

        for (Entry<C, List<E>> variable : generatedCases.entrySet()) {
            result.put(variable.getKey(), variable.getValue().get(i));
        }

        return result;
    }

    /**
     *
     * @param baseData describing of parameters and its possible values
     */
    public PairwiseGenerator(@NonNull final Map<C, List<E>> baseData) {
        this.baseData = baseData;
        validate(baseData);
        this.baseDataIndex = new PairwiseIndex<>(baseData);
        this.generatedCases = generate();
    }

    private void validate(@NonNull Map<C, List<E>> validatingData) {
        validatingData.entrySet().stream().map(e -> e.getValue()).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Need at least one possible value for parameter"));
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
            if (baseData.isEmpty()) {
                returnCases = baseData;
                rowsCount = 0;
            } else if (baseData.size() == 1) {
                returnCases = baseData;
                rowsCount = returnCases.entrySet().stream().map(e -> e.getValue().size()).findFirst().orElse(0);
            } else {
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
