/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.docs.client.filter;

//tag::class[]
import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.client.ReactorHttpClient;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

@Requires(env = Environment.GOOGLE_COMPUTE)
@Filter(patterns = "/google-auth/api/**")
public class GoogleAuthFilter implements HttpClientFilter {

    private final BeanProvider<ReactorHttpClient> authClientProvider;

    public GoogleAuthFilter(BeanProvider<ReactorHttpClient> httpClientProvider) { // <1>
        this.authClientProvider = httpClientProvider;
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request,
                                                         ClientFilterChain chain) {
        return Mono.fromCallable(() -> encodeURI(request))
                .flux()
                .flatMap(uri -> authClientProvider.get().retrieve(HttpRequest.GET(uri) // <2>
                        .header("Metadata-Flavor", "Google")))
                .flatMap(t -> chain.proceed(request.bearerAuth(t)));
    }

    private String encodeURI(MutableHttpRequest<?> request) throws UnsupportedEncodingException {
        URI fullURI = request.getUri();
        String receivingURI = fullURI.getScheme() + "://" + fullURI.getHost();
        return "http://metadata/computeMetadata/v1/instance/service-accounts/default/identity?audience=" +
                URLEncoder.encode(receivingURI, "UTF-8");
    }
}
//end::class[]
