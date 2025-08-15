import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SampleClientTest {

    @Test
    void testReadNames() throws IOException {
        List<String> names = SampleClient.readNames("test-names.txt");
        assertNotNull(names);
        assertEquals(Arrays.asList("Smith", "Johnson"), names);
    }

    @Test
    void testSearchNamesAndPrintAvgTimeReturnsZeroForEmptyList() {
        double result = SampleClient.searchNamesAndPrintAvgTime(FhirContext.forR4(), List.of(), false);
        assertEquals(0, result);
    }

    @Test
    void testSearchNamesAndPrintAvgTimeWithMockedClient() {
        // Mock client
        IGenericClient clientMock = mock(IGenericClient.class, RETURNS_DEEP_STUBS);
        when(clientMock.search()
                .forResource(any(Class.class))
                .where((ICriterion<?>) any())
                .returnBundle(eq(Bundle.class))
                .execute())
                .thenReturn(new Bundle());

        // Replace client creation logic by spying FhirContext
        FhirContext contextSpy = spy(FhirContext.forR4());
        doReturn(clientMock).when(contextSpy).newRestfulGenericClient(anyString());

        double avgTime = SampleClient.searchNamesAndPrintAvgTime(contextSpy, Arrays.asList("Smith", "Johnson"), false);

        assertTrue(avgTime >= 0);
        //TODO: still to figure out why an extra interception happens on the first run
        verify(clientMock, times(3)).search();
    }
}
