import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AverageSearchTimeInterceptor implements IClientInterceptor {

    private final List<Long> times;

    public AverageSearchTimeInterceptor() {
        times = new ArrayList<>();
    }

    @Override
    public void interceptRequest(IHttpRequest iHttpRequest) {
    }

    @Override
    public void interceptResponse(IHttpResponse iHttpResponse) throws IOException {
            times.add(iHttpResponse.getRequestStopWatch().getMillis());
    }

    public double getAverageMillis() {
        return times.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }
}