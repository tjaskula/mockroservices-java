//   Copyright © 2017 Vaughn Vernon. All rights reserved.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package co.vaughnvernon.mockroservices.journal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import co.vaughnvernon.mockroservices.journal.EntryBatch;
import co.vaughnvernon.mockroservices.journal.JournalPublisher;
import co.vaughnvernon.mockroservices.journal.Journal;
import co.vaughnvernon.mockroservices.messagebus.Message;
import co.vaughnvernon.mockroservices.messagebus.MessageBus;
import co.vaughnvernon.mockroservices.messagebus.Subscriber;
import co.vaughnvernon.mockroservices.messagebus.Topic;

public class JournalPublisherTest {

  @Test
  public void testJournalPublisher() throws Exception {
    final Journal journal = Journal.open("test-ej");
    final MessageBus messageBus = MessageBus.start("test-bus");
    final Topic topic = messageBus.openTopic("test-topic");
    JournalPublisher journalPublisher =
        JournalPublisher.using(journal.name(), messageBus.name(), topic.name());
    
    final TestSubscriber subscriber = new TestSubscriber();
    topic.subscribe(subscriber);
    
    final EntryBatch batch1 = new EntryBatch();
    for (int idx = 0; idx < 3; ++idx) {
      batch1.addEntry("test1type", "test1instance" + idx);
    }
    journal.write("test1", 0, batch1);
    
    final EntryBatch batch2 = new EntryBatch();
    for (int idx = 0; idx < 3; ++idx) {
      batch2.addEntry("test2type", "test2instance" + idx);
    }
    journal.write("test2", 0, batch2);

    subscriber.waitForExpectedMessages(6);
    
    topic.close();

    journalPublisher.close();
    
    assertEquals(6, subscriber.handledMessages.size());
  }

  private class TestSubscriber implements Subscriber {
    private final List<Message> handledMessages = new ArrayList<>();
    
    public void handle(final Message message) {
      handledMessages.add(message);
    }
    
    public void waitForExpectedMessages(final int count) throws Exception {
      for (int idx = 0; idx < 100; ++idx) {
        if (handledMessages.size() == count) {
          break;
        } else {
          Thread.sleep(100L);
        }
      }
    }
  }
}
