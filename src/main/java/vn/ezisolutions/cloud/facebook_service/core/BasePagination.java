package vn.ezisolutions.cloud.facebook_service.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasePagination<T> implements Serializable {
    private Integer current_page;
    private List<T> data;
    private Integer last_page;
    private Long total;

    public BasePagination(Page<T> page) {
        this.current_page = page.getPageable().getPageNumber() + 1;
        this.last_page = page.nextOrLastPageable().getPageNumber() + 1;
        this.total = page.getTotalElements();
        this.data = page.getContent();
    }
}
