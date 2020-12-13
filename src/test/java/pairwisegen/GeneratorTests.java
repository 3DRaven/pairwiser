package pairwisegen;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.abslab.lib.pairwise.gen.PairwiseGenerator;

import provider.BaseDataArgumentsProvider;

class GeneratorTests {
	
	public GeneratorTests() {
		// TODO Auto-generated constructor stub
	}

	@ParameterizedTest
	@DisplayName("Check mapping from source data to result")
	@ArgumentsSource(BaseDataArgumentsProvider.class)
	void pairwiseVariantsTest(Map<String, List<Object>> src, Map<String, List<Object>> exc) {
		PairwiseGenerator<String, Object> gen = new PairwiseGenerator<>(src);
		assertThat(gen.getGenaratedCases()).containsAllEntriesOf(exc);
	}

}
