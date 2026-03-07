package swd.coiviet.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddQuizQuestionBulkRequest {
    @NotEmpty(message = "Danh sách câu hỏi không được rỗng")
    @Valid
    private List<AddQuizQuestionRequest> questions;
}
