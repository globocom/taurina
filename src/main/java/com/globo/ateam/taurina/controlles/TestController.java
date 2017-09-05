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

package com.globo.ateam.taurina.controlles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.ateam.taurina.model.Scenario;
import com.globo.ateam.taurina.services.FilesService;
import com.globo.ateam.taurina.services.QueueExecutorService;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/test")
public class TestController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    private final FilesService filesService;
    private final QueueExecutorService queueExecutorService;

    @Autowired
    public TestController(FilesService filesService, QueueExecutorService queueExecutorService) {
        this.filesService = filesService;
        this.queueExecutorService = queueExecutorService;
    }

    @PostMapping(consumes = { "application/json" })
    public ResponseEntity<?> create(@RequestBody Scenario scenario, HttpServletRequest request) throws IOException {
        log.info("POST /test (content-length: " + request.getContentLengthLong() + ")");
        long id = filesService.nextId();
        queueExecutorService.put(id, scenario);
        final URI locationURI = URI.create(request.getRequestURL().toString().replaceAll("/$", "") + "/" + id);
        return ResponseEntity.created(locationURI).build();
    }

    @GetMapping
    public ResponseEntity<?> getTests(final HttpServletRequest request) throws IOException {
        log.info("GET /test");
        Path tmpPath = Paths.get(filesService.tmpDir());
        if (Files.exists(tmpPath)) {
            final String requestUrl = request.getRequestURL().toString().replaceAll("/$", "");
            final Stream<String> listOfThanosTmpDir = Files.list(tmpPath)
                    .filter(f -> Files.isDirectory(f))
                    .map(s -> requestUrl + "/" + s.toString().replaceAll(".*/", ""));
            return ResponseEntity.ok(mapper.writeValueAsString(listOfThanosTmpDir.collect(Collectors.toList())));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/{testId}")
    public ResponseEntity<?> getTest(@PathVariable("testId") Long testId) throws IOException {
        log.info("GET /test/" + testId);
        String resultFile = filesService.pathResultFile(testId);
        if (Files.exists(Paths.get(resultFile))) {
            return ResponseEntity.ok(Files.readAllBytes(Paths.get(resultFile)));
        }
        if (Files.exists(Paths.get(filesService.pathIdDirectory(testId)))) {
            return ResponseEntity.ok(ImmutableMap.of("status", "still running"));
        }
        return ResponseEntity.notFound().build();
    }

}
