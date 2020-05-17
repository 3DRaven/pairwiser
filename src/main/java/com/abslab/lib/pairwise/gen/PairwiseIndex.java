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

import com.abslab.lib.pairwise.gen.PairwiseIndex.Pair;
import com.abslab.lib.pairwise.gen.PairwiseIndex.PrettyPrintedMap;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PairwiseIndex<C, E> {

	private Map<Integer, List<Integer>> index = new PrettyPrintedMap<>(new LinkedHashMap<>());
	private List<Integer> indexKeys;
	private int rightColumnName;
	private PrettyPrintedMap<Pair<Integer>, List<Pair<Integer>>> allPossiblePairs = new PrettyPrintedMap<>(
			new HashMap<>());
	private PrettyPrintedMap<Pair<Integer>, Set<Pair<Integer>>> removedPairs = new PrettyPrintedMap<>(new HashMap<>());
	private int removedPairsCount = 0;
	private Set<Pair<Integer>> finalPairwiseIndexColumns;
	private PrettyPrintedMap<Integer, List<Integer>> finalPairwiseIndex = new PrettyPrintedMap<>(new LinkedHashMap<>());

	public PairwiseIndex(Map<C, List<E>> baseData) {
		this.index = createIndex(baseData);
		indexKeys = Collections.unmodifiableList(new ArrayList<>(index.keySet()));
		generateAllPairs();
	}

	// Создаем эквивалентное представление данных
	private PrettyPrintedMap<Integer, List<Integer>> createIndex(Map<C, List<E>> baseData) {
		log.info("Start createIndex()");

		ArrayList<C> baseDataColumnNames = new ArrayList<>(baseData.keySet());

		PrettyPrintedMap<Integer, List<Integer>> index = new PrettyPrintedMap<>(new LinkedHashMap<>());
		for (int i = 0; i < baseData.size(); i++) {
			index.put(i, Collections.unmodifiableList(IntStream
					.range(0, baseData.get(baseDataColumnNames.get(i)).size()).boxed().collect(Collectors.toList())));
		}

		// Сортируем от длиннейшей колонки к коротким
		index = index.getSorted();

		log.debug("Index is created {}", index);

		return index;
	}

	@Override
	public String toString() {
		return finalPairwiseIndex.toString();
	}

	/**
	 * Просто заполняем случайными значениями не важные пустоты так как все пары уже
	 * покрыты
	 */
	public void fillNulls() {
		log.info("Start fillNulls()");
		log.debug("Final index state  {}", finalPairwiseIndex);
		finalPairwiseIndex.entrySet().stream().map(e -> e.getValue()).forEach(c -> {
			int j = 0;
			for (int i = 0; i < c.size(); i++) {
				if (null == c.get(i)) {
					c.set(i, index.get(0).get(0));
					j++;
				}
			}
		});

		log.debug("Final pairwise index {}", finalPairwiseIndex);
	}

	public void fillStart() {
		// Добавляем все паросочетания первых двух колонок целиком
		int firstColumnName = addColumnToRight();
		int secondColumnName = addColumnToRight();
		Pair<Integer> addedColumns = Pair.of(firstColumnName, secondColumnName);
		List<Pair<Integer>> allPairs = getAllPairsOfColumn(addedColumns);
		allPairs.stream().forEach(p -> {
			addPairToRow(addedColumns, p);
		});
	}

	/**
	 * Генерирует на основе исходных данных и индекса набор тестов
	 * 
	 * @return
	 */
	public Map<C, List<E>> map(Map<C, List<E>> baseData) {
		log.info("Start map()");
		if (getNotRemovedPairs().entrySet().stream().mapToInt(e -> e.getValue().size()).sum() != 0) {
			throw new IllegalStateException(
					String.format("Not all pairs covered, all pairs [%s], removed [%s], base data [%s]"));
		}

		log.info("All pairs covered");

		ArrayList<C> baseDataKeys = new ArrayList<>(baseData.keySet());

		PrettyPrintedMap<C, List<E>> cases = new PrettyPrintedMap<>(new LinkedHashMap<>());
		for (Integer indexColumnName : indexKeys) {
			if (finalPairwiseIndex.containsKey(indexColumnName)) {
				for (Integer indexValue : finalPairwiseIndex.get(indexColumnName)) {
					C baseColumn = baseDataKeys.get(indexColumnName);
					E baseValue = baseData.get(baseColumn).get(indexValue);
					cases.computeIfAbsent(baseColumn, k -> new ArrayList<>()).add(baseValue);
				}
			} else {
				log.warn("This column is not in generated set {}", baseDataKeys.get(indexColumnName));
			}
		}

		log.debug("Cases generated {}", cases);

		int size = finalPairwiseIndex.getMaxColumnSize();

		if (cases.entrySet().stream().map(c -> c.getValue().size()).filter(s -> !s.equals(size)).findAny()
				.isPresent()) {
			log.error("Cases generated {}", cases);
			throw new IllegalStateException(String.format("We have broken column in index %s", finalPairwiseIndex));
		}

		log.info("Number of generated cases: {}",
				Optional.ofNullable(finalPairwiseIndex.get(indexKeys.get(0))).map(v -> v.size()).orElse(0));
		return cases;

	}

	/**
	 * Добавляет колонку справа
	 * 
	 * @return возвращает ее имя в индексе
	 */
	public Integer addColumnToRight() {
		rightColumnName = indexKeys.get(finalPairwiseIndex.size());
		if (index.get(rightColumnName).size() > 0) {
			finalPairwiseIndex.put(rightColumnName, new ArrayList<>());
			finalPairwiseIndexColumns = calculateColumnsPairs(finalPairwiseIndex.keySet());
			return rightColumnName;
		} else {
			throw new IllegalArgumentException(String.format("We have empty column [%d] for cover", rightColumnName));
		}
	}

	public void addColumn() {
		addColumnToRight();
		List<Integer> columnCandidates = new ArrayList<>(getCandidatesToRight());

		// Сначала добавим всех кандидатов по очереди без проверок
		for (Integer v : columnCandidates) {
			addValueToRight(v);
		}

		// Расширяем наш набор тестов справа
		for (int i = columnCandidates.size(); i < finalPairwiseIndex.getMaxColumnSize(); i++) {
			Integer value = columnCandidates.stream().max(this::compareCandidates)
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
	 * Добавляет в крайнюю правую колонку одно значение
	 * 
	 * @param value
	 */
	public void addValueToRight(Integer value) {
		final List<Integer> c = finalPairwiseIndex.get(rightColumnName);
		if (c.size() < finalPairwiseIndex.getMaxColumnSize()) {
			c.add(value);
		} else {
			throw new IndexOutOfBoundsException(String.format("We have maximum of rows:\n%s", index));
		}
		// Добавим все новые пары в удаленные, так как мы их храним в Set, то можно не
		// париться с перепроверкой дублей
		final int row = c.size() - 1;

		addAllRemovedPairs(rightColumnName, row);
	}

	public List<Pair<Integer>> getAllPairsOfColumn(Pair<Integer> columnName) {
		return allPossiblePairs.get(columnName);
	}

	/**
	 * 
	 * @return все ли возможные пары удалены из данного набора тестов
	 */
	public boolean isNeedRows() {
		int allNotRemoved = allPossiblePairs.entrySet().stream().filter(e -> removedPairs.containsKey(e.getKey()))
				.mapToInt(e -> e.getValue().size()).sum();
		return removedPairsCount != allNotRemoved;
	}

	public boolean isRemovedAll() {
		int allPairsCount = allPossiblePairs.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
		return removedPairsCount == allPairsCount;
	}

	/**
	 * Добавляет все паросочетания с самой правой колонкой в заданной строке в
	 * удаленные
	 * 
	 * @param row
	 */
	private void addAllRemovedPairs(int column, int row) {
		finalPairwiseIndexColumns.stream().filter(p -> p.getSecond().equals(column)).forEach(p -> {
			List<Integer> firstValues = finalPairwiseIndex.get(p.getFirst());
			List<Integer> secondValues = finalPairwiseIndex.get(p.getSecond());
			Integer firstValue = firstValues.get(row);
			Integer secondValue = secondValues.get(row);
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
	 * Посчитает сколько пар будет удалено при добавлении этого значения справа
	 * 
	 * @param value
	 * @return
	 */
	private Pair<Integer> getRemovedPairs(Integer value) {
		AtomicInteger removed = new AtomicInteger(0);
		AtomicInteger notRemovedInColumns = new AtomicInteger(0);

		// Выберем только те пары где справа самая правая колонка, так как только пары с
		// ней нас волнуют, они еще не удалены
		finalPairwiseIndexColumns.stream().filter(p -> p.getSecond().equals(rightColumnName)).forEach(p -> {
			if (!isRemoved(p, Pair.of(getValue(getRightColumnRow(), p.getFirst()), value))) {
				removed.incrementAndGet();
				notRemovedInColumns.addAndGet(countNotRemoved(p));
			}
		});
		return Pair.of(removed.get(), notRemovedInColumns.get());
	}

	private Integer getValue(int row, Integer columnName) {
		List<Integer> values = finalPairwiseIndex.get(columnName);
		return values.get(row);
	}

	private int getRightColumnRow() {
		return finalPairwiseIndex.get(rightColumnName).size();
	}

	public int compareCandidates(Integer c1, Integer c2) {

		if (c1.equals(c2)) {
			return 0;
		}

		Pair<Integer> rc1 = getRemovedPairs(c1);
		Pair<Integer> rc2 = getRemovedPairs(c2);

		// Если первый кандидат удаляет больше пар
		if (rc1.getFirst() > rc2.getFirst()) {
			return 1;
			// Если кандидаты удаляют одинаковое количество пар
		} else if (rc1.getFirst() == rc2.getFirst()) {
			// Если в неудаленных у первого кандидата меньше чем у второго, то он
			// предпочтительнее
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
	 * Удалена ли эта пара из этой колонки
	 * 
	 * @param columnName
	 * @param pair
	 * @return
	 */
	private boolean isRemoved(Pair<Integer> columnName, Pair<Integer> pair) {
		return removedPairs.containsKey(columnName) && removedPairs.get(columnName).contains(pair);
	}

	private int countNotRemoved(Pair<Integer> columnName) {
		return (int) allPossiblePairs.get(columnName).stream().filter(p -> !isRemoved(columnName, p)).count();
	}

	/**
	 * 
	 * @return список возможных значений для правой колонки
	 */
	public List<Integer> getCandidatesToRight() {
		return index.get(rightColumnName);
	}

	public PrettyPrintedMap<Pair<Integer>, Set<Pair<Integer>>> getNotRemovedPairs() {
		PrettyPrintedMap<Pair<Integer>, Set<Pair<Integer>>> notRemovedPairs = new PrettyPrintedMap<>(new HashMap<>());
		removedPairs.entrySet().stream().forEach(e -> {
			allPossiblePairs.get(e.getKey()).stream().filter(v -> !e.getValue().contains(v)).forEach(v -> {
				notRemovedPairs.computeIfAbsent(e.getKey(), k -> new HashSet<>()).add(v);
			});
		});
		return notRemovedPairs;
	}

	/**
	 * Добавит новую строку в массив, заполнив все кроме переданных значения null
	 * 
	 * @param columnsNames
	 * @param values
	 */
	public void addPairToRow(Pair<Integer> columnsNames, Pair<Integer> values) {
		for (Entry<Integer, List<Integer>> column : finalPairwiseIndex.entrySet()) {
			if (column.getKey().equals(columnsNames.getFirst())) {
				column.getValue().add(values.getFirst());
			} else if (column.getKey().equals(columnsNames.getSecond())) {
				column.getValue().add(values.getSecond());
			} else {
				column.getValue().add(null);
			}
		}

		addAllRemovedPairs(columnsNames.getSecond(), finalPairwiseIndex.get(columnsNames.getSecond()).size() - 1);
	}

//	public void addRow() {
//		Map<Pair<Integer>, Set<Pair<Integer>>> rowsCandidates = getNotRemovedPairs();
//		for (Map.Entry<Pair<Integer>, Set<Pair<Integer>>> rowCandidates : rowsCandidates.entrySet()) {
//			for (Pair<Integer> candidate : rowCandidates.getValue()) {
//				addPairToRow(rowCandidates.getKey(), candidate);
//			}
//		}
//	}

	public void addRow() {
		Map<Pair<Integer>, Set<Pair<Integer>>> notRemoved = getNotRemovedPairs();

		// Перебираем все колонки по очереди
		notRemoved.entrySet().stream().forEach(c -> {
			// Перебираем все пары по очереди
			for (Pair<Integer> pair : c.getValue()) {
				List<Integer> firstColumn = finalPairwiseIndex.get(c.getKey().getFirst());
				List<Integer> secondColumn = finalPairwiseIndex.get(c.getKey().getSecond());
				boolean found = false;
				// Берем вторую колонку как основу для перебора позиций в колонке
				for (int i = 0; i < secondColumn.size(); i++) {
					Integer firstValue = firstColumn.get(i);
					Integer secondValue = secondColumn.get(i);
					// Если на втором месте в тестовом наборе значение совпадает с значением из
					// неудаленной пары
					if (null != secondValue && secondValue.equals(pair.getSecond())) {
						// На первом пусто
						if (null == firstValue) {
							// Заменим в тестовом наборе первое на неудаленное
							firstColumn.set(i, pair.getFirst());
							addPairToRemoved(c.getKey(), pair);
							found = true;
							break;
						}
					}

					if (null == firstValue && null == secondValue) {
						// Заменим в тестовом наборе первое на неудаленное
						firstColumn.set(i, pair.getFirst());
						secondColumn.set(i, pair.getSecond());
						addPairToRemoved(c.getKey(), pair);
						found = true;
						break;
					}
				}

				if (!found) {
					// Если такой пары в тесте не нашлось, просто переберем всю строку и в этой
					// колонке добавим значения, в остальных пусто
					firstColumn.add(pair.getFirst());
					secondColumn.add(pair.getSecond());
					addPairToRemoved(c.getKey(), pair);
					for (int j = 0; j < finalPairwiseIndex.keySet().size(); j++) {
						Integer columnName = indexKeys.get(j);
						if (columnName != c.getKey().getFirst() && columnName != c.getKey().getSecond()) {
							finalPairwiseIndex.get(columnName).add(null);
						}
					}
				}
			}
		});

		log.trace("After row adding state {}", finalPairwiseIndex);
	}

	private void addPairToRemoved(Pair<Integer> columnName, Pair<Integer> pair) {
		if (removedPairs.computeIfAbsent(columnName, k -> new HashSet<>()).add(pair)) {
			removedPairsCount++;
		}
	}

	/**
	 * Генерит все возможные паросочетания колонок в индексе
	 */
	private void generateAllPairs() {
		log.info("Start generateAllPairs()");
		final Set<Pair<Integer>> columnPairs = calculateColumnsPairs(index.keySet());

		columnPairs.stream().forEach(p -> {

			List<Integer> firstColumn = index.get(p.getFirst());
			List<Integer> secondColumn = index.get(p.getSecond());

			// Колонки отсортированы, потому мы можем спокойно брать паросочетания от первой
			// к последней, паросочетания нужны без возврата для колонок 1,2,3 1-2 1-3 2-3
			List<Pair<Integer>> pairs = new ArrayList<>();
			for (int i = 0; i < firstColumn.size(); i++) {
				for (int j = 0; j < secondColumn.size(); j++) {
					pairs.add(Pair.of(firstColumn.get(i), secondColumn.get(j)));
				}
			}
			allPossiblePairs.put(p, Collections.unmodifiableList(pairs));
		});

		log.trace("All pairs is generated {}", allPossiblePairs);
	}

	/**
	 * Генерит все возможные сочетания номеров колонок в индексе
	 * 
	 * @return
	 */
	private Set<Pair<Integer>> calculateColumnsPairs(Set<Integer> columnNames) {
		final Set<Pair<Integer>> possiblePairs = new HashSet<>();

		final List<Integer> keys = new ArrayList<>(columnNames);

		for (int i = 0; i < columnNames.size(); i++) {
			for (int j = i + 1; j < columnNames.size(); j++) {
				possiblePairs.add(Pair.of(keys.get(i), keys.get(j)));
			}
		}
		return Collections.unmodifiableSet(possiblePairs);
	}

	@Value
	public static class Pair<E> {
		private E first;
		private E second;

		public static <E> Pair<E> of(E f, E s) {
			return new Pair<E>(f, s);
		}

		@Override
		public String toString() {
			return "[" + Objects.toString(first, "-") + "," + Objects.toString(second, "-") + "]";
		}
	}

	@RequiredArgsConstructor
	public static class PrettyPrintedMap<C, E extends Collection<?>> implements Map<C, E> {

		@Delegate
		private final Map<C, E> backMap;

		public PrettyPrintedMap<C, E> getSorted() {
			return new PrettyPrintedMap<>(backMap.entrySet().stream().sorted(Map.Entry.comparingByValue((v1, v2) -> {
				return -Integer.compare(v1.size(), v2.size());
			})).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)));
		}

		public int getMaxColumnSize() {
			return backMap.entrySet().stream().mapToInt(c -> c.getValue().size()).max().orElse(0);
		}

		@Override
		public String toString() {
			// From java 1.5 it is optimized to StringBuilder by compiler :)
			String pretty = "{\n";
			pretty += backMap.entrySet().stream().map(column -> {
				String res = "	";
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
