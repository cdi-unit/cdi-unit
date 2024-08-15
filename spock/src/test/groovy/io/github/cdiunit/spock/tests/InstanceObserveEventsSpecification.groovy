package io.github.cdiunit.spock.tests

import io.github.cdiunit.test.beans.Qualify
import jakarta.enterprise.event.Event
import jakarta.enterprise.event.Observes
import jakarta.enterprise.inject.spi.EventMetadata
import jakarta.inject.Inject

class InstanceObserveEventsSpecification extends BaseSpecification {

    static class TestEvent {
    }

    @Inject
    Event<TestEvent> testEvent;

    int observedUnqualified;

    void observeUnqualified(@Observes TestEvent event, EventMetadata metadata) {
        observedUnqualified++;
    }

    def 'should observe unqualified event'() {
        when:
        final var expected = new TestEvent();
        testEvent.fire(expected);

        then:
        observedQualified == 0
        observedUnqualified == 1
    }

    int observedQualified;

    void observeQualified(@Observes @Qualify TestEvent event) {
        observedQualified++;
    }

    def 'should observe qualified event'() {
        when:
        final var expected = new TestEvent();
        testEvent.select(Qualify.Literal.INSTANCE).fire(expected);

        then:
        observedQualified == 1
        observedUnqualified == 1
    }


}
