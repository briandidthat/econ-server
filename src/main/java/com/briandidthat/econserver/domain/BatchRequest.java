package com.briandidthat.econserver.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class BatchRequest {
    @Size(min = 2, max = 5)
    @JsonProperty("requests")
    private List<Request> requests;

    public BatchRequest() {
    }

    public BatchRequest(List<Request> requests) {
        this.requests = requests;
    }

    public List<Request> getRequests() {
        return requests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchRequest that = (BatchRequest) o;
        return Objects.equals(requests, that.requests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requests);
    }

    @Override
    public String toString() {
        return "BatchRequest{" + "requests=" + requests + '}';
    }
}
