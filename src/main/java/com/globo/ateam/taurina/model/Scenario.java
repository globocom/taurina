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

package com.globo.ateam.taurina.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.globo.ateam.taurina.model.serializers.ScenarioDeserializer;
import com.globo.ateam.taurina.model.serializers.ScenarioSerializer;

@JsonSerialize(using = ScenarioSerializer.class)
@JsonDeserialize(using = ScenarioDeserializer.class)
public class Scenario {

    private String domain = "localhost";
    private Integer port = 80;
    private String path = "/";
    private String method = "GET";
    private Integer loopCount = 1;
    private Integer numThreads = 1;
    private Integer rampUp = 1;
    private String statsdPrefix = "my.test";
    private String statsdServer = "127.0.0.1";
    private Integer statsdPort = 8125;
    private long duration = 1;

    public String getDomain() {
        return domain;
    }

    public Integer getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public Integer getLoopCount() {
        return loopCount;
    }

    public Integer getNumThreads() {
        return numThreads;
    }

    public long getDuration() {
        return duration;
    }

    public Integer getRampUp() {
        return rampUp;
    }

    public String statsdPrefix() {
        return statsdPrefix;
    }

    public String statsdServer() {
        return statsdServer;
    }

    public Integer statsdPort() {
        return statsdPort;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setLoopCount(Integer loopCount) {
        this.loopCount = loopCount;
    }

    public void setNumThreads(Integer numThreads) {
        this.numThreads = numThreads;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setRampUp(Integer rampUp) {
        this.rampUp = rampUp;
    }

    public void setStatsdPrefix(String statsdPrefix) {
        this.statsdPrefix = statsdPrefix;
    }

    public void setStatsdServer(String statsdServer) {
        this.statsdServer = statsdServer;
    }

    public void setStatsdPort(Integer statsdPort) {
        this.statsdPort = statsdPort;
    }
}
