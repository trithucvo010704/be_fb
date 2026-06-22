package vn.ezisolutions.cloud.facebook_service.gateway.facebook;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.net.URI;
import java.util.Map;

@HttpExchange
public interface FacebookPublishGateway {

    @PostExchange("/{pageId}/{edge}")
    Map<String, Object> publishToEdge(
            @PathVariable("pageId") String pageId,
            @PathVariable("edge") String edge,
            @RequestBody Map<String, Object> body
    );

    @PostExchange(value = "/{pageId}/{edge}", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> publishFormData(
            @PathVariable("pageId") String pageId,
            @PathVariable("edge") String edge,
            @RequestBody MultiValueMap<String, String> body
    );

    @PostExchange("/{postId}")
    Map<String, Object> updatePost(
            @PathVariable("postId") String postId,
            @RequestBody Map<String, Object> body
    );

    @PostExchange
    void uploadVideoBinary(
            URI uri,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("offset") String offset,
            @RequestHeader("file_size") String fileSize,
            @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
            @RequestBody FileSystemResource file
    );

    @PostExchange
    void uploadVideoByUrl(
            URI uri,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("file_url") String fileUrl
    );
}
