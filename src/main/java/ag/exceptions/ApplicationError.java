package ag.exceptions;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@RequiredArgsConstructor
@ResponseBody
@Data
public class ApplicationError {
    private Integer status;
    private String message;
    private Date timestamp;

    public ApplicationError(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = new Date();
    }
}
