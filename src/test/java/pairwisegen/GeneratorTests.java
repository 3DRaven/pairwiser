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
    void testPairwiseVariants(final Map<String, List<Object>> src, final Map<String, List<Object>> exc) {
        final PairwiseGenerator<String, Object> gen = new PairwiseGenerator<>(src);
        assertThat(gen.getGenaratedCases()).as("Check all elements of response is allowed")
                .overridingErrorMessage("We have not allowed response").containsAllEntriesOf(exc);
    }

}
