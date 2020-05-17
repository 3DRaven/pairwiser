package com.abslab.lib.pairwise.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.abslab.lib.pairwise.gen.PairwiseIndex.PrettyPrintedMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PairwiseGenerator<C, E> implements Iterator<List<E>> {

	private Map<C, List<E>> baseData;
	private PairwiseIndex<C, E> baseDataIndex;
	private Map<C, List<E>> generatedCases;
	private int currentRow;
	private int rowsCount;

	public PairwiseGenerator(Map<C, List<E>> baseData) {
		this.baseData = baseData;
		this.baseDataIndex = new PairwiseIndex<>(baseData);
		this.generatedCases = generate();
	}

	public Map<C, List<E>> getGenaratedCases() {
		return Collections.unmodifiableMap(generatedCases);
	}

	private Map<C, List<E>> generate() {

		if (null != generatedCases) {
			return generatedCases;
		}

		baseDataIndex.fillStart();
		// Горизонтальны рост
		while (!baseDataIndex.isRemovedAll()) {
			log.info("Add column");
			baseDataIndex.addColumn();
			// Вертикальный рост
			while (baseDataIndex.isNeedRows()) {
				log.info("Add row");
				baseDataIndex.addRow();
			}
		}
		baseDataIndex.fillNulls();
		generatedCases = baseDataIndex.map(baseData);
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

	public Stream<List<E>> stream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, 0), false);
	}
}
