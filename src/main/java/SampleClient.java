import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

import java.util.Comparator;
import java.util.List;

public class SampleClient {

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();

        // Sort the response by patience's first name since the order is not guarantied
        sortBundleEntriesByFirstName(response);
        // Process the response for printing names and DOBs
        printPatientsNameAndDOB(response);
    }

    public static void sortBundleEntriesByFirstName(Bundle bundle) {
        if (bundle == null || bundle.getEntry() == null || bundle.getEntry().isEmpty()) {
            return;
        }

        List<Bundle.BundleEntryComponent> entries = bundle.getEntry();

        // Sort the entries using a custom Comparator
        entries.sort(new Comparator<Bundle.BundleEntryComponent>() {
            @Override
            public int compare(Bundle.BundleEntryComponent entryFirst, Bundle.BundleEntryComponent entrySecond) {

                Patient patientFirst = (Patient) entryFirst.getResource();
                Patient patientSecond = (Patient) entrySecond.getResource();

                if (patientFirst == null && patientSecond == null) {
                    return 0;
                } else if (patientFirst == null) {
                    return 1;
                } else if (patientSecond == null) {
                    return -1;
                }

                String nameFirst = patientFirst.getNameFirstRep().getGivenAsSingleString();
                String nameSecond = patientSecond.getNameFirstRep().getGivenAsSingleString();

                return nameFirst.compareTo(nameSecond);
            }
        });
    }

    public static void printPatientsNameAndDOB(Bundle bundle) {
        bundle.getEntry().stream()
                .map(entry -> (Patient) entry.getResource())
                .forEach(patient -> {
                    // the name may consist of multiple names
                    String name = patient.getName().stream()
                            .map(HumanName::getNameAsSingleString)
                            .findFirst()
                            .orElse("NO NAME");

                    String dateOfBirth = patient.hasBirthDate() ? patient.getBirthDate().toString() : "UKNOWN";
                    System.out.println(name + ", " + dateOfBirth);
                });
    }
}
