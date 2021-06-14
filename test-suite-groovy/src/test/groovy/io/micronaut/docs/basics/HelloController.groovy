/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.docs.basics

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.client.ReactorHttpClient
import io.micronaut.http.client.annotation.Client
import reactor.core.publisher.Mono

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpStatus.CREATED
import static io.micronaut.http.MediaType.TEXT_PLAIN
// end::imports[]

@Requires(property = "spec.name", value = "HelloControllerSpec")
@Controller("/")
class HelloController {

    private final ReactorHttpClient httpClient

    HelloController(@Client("/endpoint") ReactorHttpClient httpClient) {
        this.httpClient = httpClient
    }

    // tag::nonblocking[]
    @Get("/hello/{name}")
    Mono<String> hello(String name) { // <1>
        httpClient.retrieve( GET("/hello/" + name) )
                  .firstElement() // <2>
    }
    // end::nonblocking[]

    @Get("/endpoint/hello/{name}")
    String helloEndpoint(String name) {
        "Hello $name"
    }

    // tag::json[]
    @Get("/greet/{name}")
    Message greet(String name) {
        new Message("Hello $name")
    }
    // end::json[]

    @Post("/greet")
    @Status(CREATED)
    Message echo(@Body Message message) {
        message
    }

    @Post(value = "/hello", consumes = TEXT_PLAIN, produces = TEXT_PLAIN)
    @Status(CREATED)
    String echoHello(@Body String message) {
        message
    }
}
