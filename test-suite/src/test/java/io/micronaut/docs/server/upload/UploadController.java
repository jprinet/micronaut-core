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
package io.micronaut.docs.server.upload;

// tag::class[]
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;

import static io.micronaut.http.HttpStatus.CONFLICT;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
import static io.micronaut.http.MediaType.TEXT_PLAIN;

@Controller("/upload")
public class UploadController {

    @Post(value = "/", consumes = MULTIPART_FORM_DATA, produces = TEXT_PLAIN) // <1>
    public Mono<HttpResponse<String>> upload(StreamingFileUpload file) { // <2>

        File tempFile;
        try {
            tempFile = File.createTempFile(file.getFilename(), "temp");
        } catch (IOException e) {
            return Mono.error(e);
        }
        Publisher<Boolean> uploadPublisher = file.transferTo(tempFile); // <3>

        return Mono.from(uploadPublisher)  // <4>
            .map(success -> {
                if (success) {
                    return HttpResponse.ok("Uploaded");
                } else {
                    return HttpResponse.<String>status(CONFLICT)
                                       .body("Upload Failed");
                }
            });
    }
}
// end::class[]
