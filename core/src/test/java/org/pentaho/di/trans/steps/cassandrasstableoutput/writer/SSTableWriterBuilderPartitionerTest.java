/*!
 * Copyright 2026 Hitachi Vantara.  All rights reserved.
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
 *
 */

package org.pentaho.di.trans.steps.cassandrasstableoutput.writer;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class SSTableWriterBuilderPartitionerTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private String partitioner( String yaml ) throws Exception {
    File file = folder.newFile();
    Files.write( file.toPath(), yaml.getBytes( StandardCharsets.UTF_8 ) );
    return new SSTableWriterBuilder().withConfig( file.getAbsolutePath() ).getPartitionerClass();
  }

  @Test
  public void readsThePartitionerFromCassandraYaml() throws Exception {
    assertEquals( "org.apache.cassandra.dht.RandomPartitioner",
      partitioner( "cluster_name: 'Test Cluster'\npartitioner: org.apache.cassandra.dht.RandomPartitioner\n"
        + "seed_provider:\n  - class_name: org.apache.cassandra.locator.SimpleSeedProvider\n" ) );
  }

  @Test
  public void defaultsToMurmur3() throws Exception {
    assertEquals( SSTableWriterBuilder.DEFAULT_PARTITIONER, partitioner( "cluster_name: 'Test Cluster'\n" ) );
  }

  @Test( expected = ConfigurationException.class )
  public void refusesArbitraryTypes() throws Exception {
    // the safe parse must not instantiate classes named in the file
    partitioner( "partitioner: !!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[]]]\n" );
  }

  @Test( expected = ConfigurationException.class )
  public void missingFile() throws Exception {
    new SSTableWriterBuilder().withConfig( "/no/such/cassandra.yaml" ).getPartitionerClass();
  }
}
