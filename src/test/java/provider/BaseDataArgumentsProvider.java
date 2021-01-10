package provider;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BaseDataArgumentsProvider implements ArgumentsProvider {

    private final ObjectMapper mapper = new ObjectMapper();

    public BaseDataArgumentsProvider() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
        try {

            final List<Map<String, Map<String, List<Object>>>> loadedData = mapper.readValue(
                    new URL("file:src/test/resources/pairwise-data.json"),
                    new TypeReference<List<Map<String, Map<String, List<Object>>>>>() {
                    });

            return loadedData.stream().map(test -> Arguments.of(test.get("source"), test.get("expected")))
                    .collect(Collectors.toList()).stream();
        } catch (final IOException e) {
            throw new IllegalStateException(String.format("Unable to load json with tests data with exception %s", e));
        }
    }
}
