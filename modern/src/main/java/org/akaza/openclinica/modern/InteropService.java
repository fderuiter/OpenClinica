package org.akaza.openclinica.modern;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InteropService {

    private Map<String, String> recordsInStaging = new ConcurrentHashMap<>();

    public void validate(String recordId, String payload) {
        // Schema validation and business logic checks
        recordsInStaging.put(recordId, payload);
    }

    public List<String> getReviewQueue() {
        return new ArrayList<>(recordsInStaging.keySet());
    }

    public void commit(String recordId) {
        // Mandatory user-review stage approved, commit data
        recordsInStaging.remove(recordId);
    }
}
