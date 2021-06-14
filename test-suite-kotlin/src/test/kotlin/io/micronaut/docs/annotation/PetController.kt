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
package io.micronaut.docs.annotation

// tag::imports[]
import io.micronaut.http.annotation.Controller
import reactor.core.publisher.Mono
// end::imports[]

// tag::class[]
@Controller("/pets")
open class PetController : PetOperations {

    override fun save(name: String, age: Int): Mono<Pet> {
        val pet = Pet()
        pet.name = name
        pet.age = age
        // save to database or something
        return Mono.just(pet)
    }
}
// end::class[]
