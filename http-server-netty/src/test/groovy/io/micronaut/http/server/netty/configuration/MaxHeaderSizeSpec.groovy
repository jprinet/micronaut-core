/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.http.server.netty.configuration

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.ReactorHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification

/**
 * @author graemerocher
 * @since 1.0
 */
class MaxHeaderSizeSpec extends Specification {

    void "test that the max header size can be configured"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [
                'micronaut.server.netty.maxHeaderSize':10
        ])
        ReactorHttpClient client = embeddedServer.applicationContext.createBean(ReactorHttpClient, embeddedServer.getURL())

        when:
        client.exchange(
                HttpRequest.GET('/max-header')
                .header("X-Foo", "Foo" * 100)
        ).blockFirst()

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.REQUEST_ENTITY_TOO_LARGE

        cleanup:
        embeddedServer.stop()
        client.stop()
    }

    @Controller('/max-header')
    static class TestController {
        @Get
        HttpStatus index() {
            HttpStatus.OK
        }
    }
}
