/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.daemon;

import static io.restassured.RestAssured.given;

import org.junit.Test;

import io.restassured.http.ContentType;

public class GuiHandlerIT extends BaseIT {

    @Test
    public void checkGui() {
        given().when().get("/").then().statusCode(200).and().contentType(ContentType.HTML);
        given().when().get("/README.md").then().statusCode(200).and().contentType("text/markdown");
        given().when().get("/unknown.md").then().statusCode(404).and().contentType(ContentType.TEXT);
    }
}
