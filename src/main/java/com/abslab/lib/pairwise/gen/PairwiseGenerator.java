package com.abslab.lib.pairwise.gen;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	private Map<C, List<E>> baseData;
	private PairwiseIndex<C, E> baseDataIndex;
	private Map<C, List<E>> generatedCases;
	private int currentRow;
	private int rowsCount;

	/**
	 * 
	 * @param baseData describing of parameters and its possible values
	 */
	public PairwiseGenerator(Map<C, List<E>> baseData) {
		this.baseData = baseData;
		this.baseDataIndex = new PairwiseIndex<>(baseData);
		this.generatedCases = generate();
	}

	public Map<C, List<E>> getGenaratedCases() {
		return Collections.unmodifiableMap(generatedCases);
	}

	/**
	 * Generation of test cases
	 * 
	 * @return
	 */
	private Map<C, List<E>> generate() {

		if (null != generatedCases) {
			return generatedCases;
		}

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
		generatedCases = baseDataIndex.map(baseData);
		// Paranoid check that all pairs is covered
		for (Entry<C, List<E>> entry : generatedCases.entrySet()) {
			rowsCount = entry.getValue().size();
			break;
		}
		return generatedCases;
	}

	@Override
	public boolean hasNext() {
		return currentRow < rowsCount;
	}

	@Override
	public List<E> next() {
		List<E> row = generatedCases.entrySet().stream().map(e -> e.getValue().get(currentRow))
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
