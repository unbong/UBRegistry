package io.unbong.ubregistry;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Description
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-20 21:32
 */
@Data
@AllArgsConstructor
public class ExceptionResponse {

    HttpStatus internalServerError;
    String msg;

}
