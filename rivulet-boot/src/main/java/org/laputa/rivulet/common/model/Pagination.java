package org.laputa.rivulet.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author JQH
 * @since 下午 3:22 22/11/10
 */
@Setter
@Getter
public class Pagination {
    private int pageSize;
    private int pageNumber;

    @JsonIgnore
    public Pageable getPageable() {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return pageRequest;
    }
}
