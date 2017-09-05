/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globo.ateam.taurina.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.globo.ateam.taurina.model.Scenario;

import java.io.IOException;

public class ScenarioSerializer extends JsonSerializer<Scenario> {
    @Override
    public void serialize(Scenario scenario, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("domain", scenario.getDomain());
        jsonGenerator.writeNumberField("port", scenario.getPort());
        jsonGenerator.writeStringField("domain", scenario.getDomain());
        jsonGenerator.writeStringField("path", scenario.getPath());
        jsonGenerator.writeStringField("method", scenario.getMethod());
        jsonGenerator.writeNumberField("loopCount", scenario.getLoopCount());
        jsonGenerator.writeNumberField("numThreads", scenario.getNumThreads());
        jsonGenerator.writeNumberField("duration", scenario.getDuration());
        jsonGenerator.writeNumberField("rampUp", scenario.getRampUp());
        jsonGenerator.writeStringField("statsdPrefix", scenario.statsdPrefix());
        jsonGenerator.writeStringField("statsdServer", scenario.statsdServer());
        jsonGenerator.writeNumberField("statsdPort", scenario.statsdPort());
        jsonGenerator.writeEndObject();
    }
}
