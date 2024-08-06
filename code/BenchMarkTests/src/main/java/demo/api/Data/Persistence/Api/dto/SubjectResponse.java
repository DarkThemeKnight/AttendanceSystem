package demo.api.Data.Persistence.Api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SubjectResponse {
    private String message;
    @JsonProperty("subject_code")
    private String subjectCode;
    @JsonProperty("subject_title")
    private String subjectTitle;
    @JsonProperty("id_lecturer_in_charge")
    private String idLecturerInCharge;
    @JsonProperty("students")
    private Set<Metadata> students = new HashSet<>();
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class Metadata{
        String studentId;
        String firstname;
        String lastname;
        boolean isSuspended;
    }
    public SubjectResponse(String message) {
        this.message = message;
    }
}

