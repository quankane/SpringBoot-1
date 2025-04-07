package group.quankane.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class ResponseSuccess extends ResponseEntity<ResponseSuccess.Payload> {
    //PUT, PATCH, DELETE
    public ResponseSuccess(HttpStatusCode status, String message) {
        super(new Payload(status.value(), message), HttpStatus.OK);
    }
    //GET lấy dữ liệu trả về cho Client, POST trả lại id cho Client để thực hiện Thêm, Cập nhật, Xóa
    public ResponseSuccess(HttpStatusCode status, String message, Object data) {
        super(new Payload(status.value(), message, data), HttpStatus.OK);
    }

    public static class Payload {
        private final int status;
        private final String message;
        private Object data;

        public Payload(int status, String message) {
            this.message = message;
            this.status = status;
        }

        public Payload(int status, String message, Object data) {
            this.data = data;
            this.message = message;
            this.status = status;
        }


        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }
}
