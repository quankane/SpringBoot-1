package group.quankane.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class PageResponse<T> implements Serializable {

    private int page;
    private int size;
    private long total;
    private T items;
}
