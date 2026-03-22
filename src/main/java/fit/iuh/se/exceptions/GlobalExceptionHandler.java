package fit.iuh.se.exceptions;

import fit.iuh.se.dtos.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Xử lý lỗi nghiệp vụ (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntime(RuntimeException ex) {
        ApiResponse res = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.badRequest().body(res);
    }

    // Xử lý tất cả lỗi khác
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneral(Exception ex) {
        ex.printStackTrace(); // 👈 in lỗi thật ra console

        ApiResponse res = new ApiResponse(false, ex.getMessage()); // 👈 hiện message thật
        return ResponseEntity.status(500).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, msg));
    }

}
