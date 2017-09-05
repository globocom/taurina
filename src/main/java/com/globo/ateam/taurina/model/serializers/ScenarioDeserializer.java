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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.globo.ateam.taurina.model.Scenario;

import java.io.IOException;

public class ScenarioDeserializer extends JsonDeserializer<Scenario> {
    @Override
    public Scenario deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);

        Scenario scenario = new Scenario();
        if (node.has("domain")) scenario.setDomain(node.get("domain").asText());
        if (node.has("port")) scenario.setPort(node.get("port").asInt());
        if (node.has("path")) scenario.setPath(node.get("path").asText());
        if (node.has("method")) scenario.setMethod(node.get("method").asText());
        if (node.has("loopCount")) scenario.setLoopCount(node.get("loopCount").asInt());
        if (node.has("numThreads")) scenario.setNumThreads(node.get("numThreads").asInt());
        if (node.has("duration")) scenario.setDuration(node.get("duration").asLong());
        if (node.has("rampUp")) scenario.setRampUp(node.get("rampUp").asInt());
        if (node.has("statsdPrefix")) scenario.setStatsdPrefix(node.get("statsdPrefix").asText());
        if (node.has("statsdServer")) scenario.setStatsdServer(node.get("statsdServer").asText());
        if (node.has("statsdPort")) scenario.setStatsdPort(node.get("statsdPort").asInt());

        return scenario;
    }
}
