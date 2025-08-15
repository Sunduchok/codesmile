import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.gclient.IQuery;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class SampleClient {

    private static final String BASE_URL = "http://hapi.fhir.org/baseR4";

    public static void main(String[] args) {
        try {
            FhirContext fhirContext = FhirContext.forR4();
            List<String> names = readNames("names.txt");

            // First run: cache allowed
            searchNamesAndPrintAvgTime(fhirContext, names, false);
            // Second run: cache allowed
            searchNamesAndPrintAvgTime(fhirContext, names, false);
            // Third run: cache disabled
            searchNamesAndPrintAvgTime(fhirContext, names, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double searchNamesAndPrintAvgTime(FhirContext fhirContext, List<String> names, boolean noCache) {
        if (names == null || names.isEmpty()) {
            System.out.println("No names provided for search.");
            return 0;
        }
        //TODO: it needs investigation, configuration changes seems do not have a visible impact on the average time
        if(noCache) {
            fhirContext.getValidationSupport().invalidateCaches();
            fhirContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        }

        IGenericClient client = fhirContext.newRestfulGenericClient(BASE_URL);
        AverageSearchTimeInterceptor avgInterceptor = new AverageSearchTimeInterceptor();
        client.registerInterceptor(avgInterceptor);

        names.forEach(name -> executeSearch(client, name, noCache));

        double avgTime = avgInterceptor.getAverageMillis();
        System.out.println("Average response time" + (noCache ? " (no cache)" : "") + ": " + avgTime + " ms");
        return avgTime;
    }

    private static void executeSearch(IGenericClient client, String name, boolean noCache) {
        IQuery<Bundle> search = client.search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value(name.toUpperCase()))
                .returnBundle(Bundle.class);

        if (noCache) {
            search.cacheControl(new CacheControlDirective().setNoCache(true));
        }
        search.execute();
    }

    public static List<String> readNames(String resourceFileName) throws IOException {
        try (InputStream inputStream = SampleClient.class.getResourceAsStream("/" + resourceFileName)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourceFileName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        }
    }
}
