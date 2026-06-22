package vn.ezisolutions.cloud.facebook_service.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.*;

@Configuration
public class FacebookClientConfig {

    @Bean("facebookHttpServiceProxyFactory")
    public HttpServiceProxyFactory facebookHttpServiceProxyFactory(RestTemplate facebookRestTemplate, FacebookProperties properties) {
        String baseUrl = properties.getGraphHost() + "/" + properties.getGraphVersion();

        RestClient restClient = RestClient.builder(facebookRestTemplate)
                .baseUrl(baseUrl)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory.builderFor(adapter).build();
    }

    private <T> T createClient(HttpServiceProxyFactory factory, Class<T> clientType) {
        return factory.createClient(clientType);
    }


    @Bean("facebookUserGateway")
    public FacebookUserGateway facebookUserGateway(HttpServiceProxyFactory factory) {
        return createClient(factory, FacebookUserGateway.class);
    }

    @Bean("facebookPageGateway")
    public FacebookPageGateway facebookPageGateway(HttpServiceProxyFactory factory) {
        return createClient(factory, FacebookPageGateway.class);
    }

    @Bean("facebookPostGateway")
    public FacebookPostGateway facebookPostGateway(HttpServiceProxyFactory factory) {
        return createClient(factory, FacebookPostGateway.class);
    }

    @Bean("facebookMessageGateway")
    public FacebookMessageGateway facebookMessageGateway(HttpServiceProxyFactory factory) {
        return createClient(factory, FacebookMessageGateway.class);
    }

    @Bean("facebookCommentGateway")
    public FacebookCommentGateway facebookCommentGateway(HttpServiceProxyFactory factory) {
        return createClient(factory, FacebookCommentGateway.class);
    }

    @Bean("facebookPublishGateway")
    public FacebookPublishGateway facebookPublishGateway(HttpServiceProxyFactory factory) {
        return createClient(factory, FacebookPublishGateway.class);
    }
}
